package org.cognito.tool.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cognito.tool.vo.CognitoVO;
import org.cognito.tool.service.CognitoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CognitoServiceImpl implements CognitoService {

    @Value("${app_client_id}")
    private String app_client_id;
    @Value("${cognito_url}")
    private String cognito_url;
    @Value("${user_name}")
    private String userName;
    @Value("${user_pwd}")
    private String userPwd;
    @Value("${aws_gateway_api}")
    private String awsGatewayApi;
    @Value("${aws_gateway_api_doc}")
    private String awsGatewayDocApi;
    @Value("${cognito_pool_public_key_url}")
    private String cognitoPoolPublicKeyUrl;


    @Override
    public List<Map<String, Object>> getChoices() {
        Map<Integer, String> map = buildChoicesList();
        return map.keySet().stream().map(key -> {
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("id",key);
            obj.put("choiceName", map.get(key));
            return obj;
        }).collect(Collectors.toList());
    }

    private Map<Integer, String> buildChoicesList(){
        Map<Integer, String> map = new HashMap<>();
        map.put(1,"Set Up MFA for User [ONE TIME STEP]");
        map.put(2,"Associate MFA Token and Get Refresh Token [REPEAT EVERY 30 DAYS]");
        map.put(3,"Get ID Token ");
        map.put(4,"Get Access Token");
        map.put(5,"Get ID Token and download Case Json data");
        map.put(6,"Get ID Token and download Case PDF Document");
        map.put(7,"Quit");
        return map;
    }


    @Override
    public Object processChoice(CognitoVO vo) {
        switch (vo.getId()) {
            case 1:
                vo.setSecretCode(this.setUpMFAForUser(vo));
                vo.setMessage(StringUtils.isEmpty(vo.getSecretCode())?"Failed to generate secret code":"");
                break;
            case 2:
                vo.setRefreshToken(this.getRefreshToken(vo));
                vo.setMessage(StringUtils.isEmpty(vo.getRefreshToken())?"Failed to generate refresh token":"");
                break;
            case 3:
                vo.setIdToken(this.getIDToken(vo));
                vo.setMessage(StringUtils.isEmpty(vo.getIdToken())?"Failed to generate ID token":"");
                break;
            case 4:
                vo.setAccessToken(this.getAccessToken(vo));
                vo.setMessage(StringUtils.isEmpty(vo.getAccessToken())?"Failed to generate access token":"");
                break;
            case 5:
                this.downloadCaseData(vo);
                break;
            case 6:
                this.downloadCaseDocument(vo);
                break;
            case 7:
            default:
                SecurityContextHolder.clearContext();
                break;
        }
        return vo;
    }

    @Override
    public byte[] downloadDocument(CognitoVO vo) throws IOException {
        String idToken = vo.getIdToken();
        String resp = "";
        try {
            //if (idToken == null || idToken.trim().equals(""))
            {
                idToken=getIDToken(vo);
            }
            resp = sendGetCaseDocumentRequest(idToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp = resp != null ? resp : "";
        return resp.getBytes();
    }


    public String setUpMFAForUser(CognitoVO vo) {
        String secretCode = "";
        try {
            String initiateAuthResponseStr = sendPostInitiateAuthCognito();
            System.out.println("response: " + initiateAuthResponseStr);
            if (initiateAuthResponseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode initiateAuthResponse = objectMapper.readValue(initiateAuthResponseStr, JsonNode.class);
                if (initiateAuthResponse != null && initiateAuthResponse instanceof NullNode == false) {
                    String initiateAuthchallengeName = initiateAuthResponse.get("ChallengeName").asText();
                    String sessionId = initiateAuthResponse.get("Session").asText();
                    System.out.println("initiateAuthchallengeName: " + initiateAuthchallengeName);
                    System.out.println("initiate Auth Response Session Id: " + sessionId);
                    if (initiateAuthchallengeName != null && initiateAuthchallengeName.trim().equals("MFA_SETUP")) {
                        System.out.println("About to get MFA secret code (associate token) ....");
                        // jsonData = httpClient.sendGetCaseDataRequest(bearerToken);
                        String associateSoftwareTokenRespStr = sendPostAssociateSoftwareToken(sessionId);
                        System.out.println("associateSoftwareTokenRespStr: " + associateSoftwareTokenRespStr);
                        JsonNode associateSoftwareTokenResponse = objectMapper.readValue(associateSoftwareTokenRespStr,
                                JsonNode.class);
                        if (associateSoftwareTokenResponse != null
                                && associateSoftwareTokenResponse instanceof NullNode == false) {
                            secretCode = associateSoftwareTokenResponse.get("SecretCode").asText();
                            String associateSoftwareTokenSessionId = associateSoftwareTokenResponse.get("Session")
                                    .asText();
                            System.out.println("associateSoftwareTokenSessionId : " + associateSoftwareTokenSessionId);
                            System.out.println(" ");
                            System.out.println("User account secret Code : " + secretCode);
                            System.out.println(
                                    "Enter the secret code on any OAUTH Authenticator Tool and generate a MFA TOKEN ");
                            System.out.println(" ");
                            System.out.println("Verify the Software Token: ");
                            //String mfaToken = scanner.nextLine();
                            String mfaToken = vo.getMfaToken();
                            String VerifySoftwareTokenRespStr = sendPostVerifySoftwareToken(mfaToken,
                                    associateSoftwareTokenSessionId);
                            System.out.println("VerifySoftwareTokenRespStr: " + VerifySoftwareTokenRespStr);
                            JsonNode verifySoftwareTokenSoftwareResponse = objectMapper.readValue(VerifySoftwareTokenRespStr,
                                    JsonNode.class);
                            if (verifySoftwareTokenSoftwareResponse != null
                                    && verifySoftwareTokenSoftwareResponse instanceof NullNode == false) {
                                String status = verifySoftwareTokenSoftwareResponse.get("Status").asText();
                                System.out.println("is MFA setup for User Account ?  " + status);
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Could not compplete MFA_SETUP for user account: ");
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return secretCode;
    }

    public String getRefreshToken(CognitoVO vo) {
        String refreshToken = "";
        try {
            String initiateAuthResponseStr = sendPostInitiateAuthCognito();
            System.out.println("response: " + initiateAuthResponseStr);
            if (initiateAuthResponseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode initiateAuthResponse = objectMapper.readValue(initiateAuthResponseStr, JsonNode.class);
                if (initiateAuthResponse != null && initiateAuthResponse instanceof NullNode == false) {
                    String initiateAuthchallengeName = initiateAuthResponse.get("ChallengeName").asText();
                    String sessionId = initiateAuthResponse.get("Session").asText();

                    //System.out.println("initiateAuthchallengeName: " + initiateAuthchallengeName);
                    //System.out.println("initiate Auth Response Session Id: " + sessionId);

                    if (initiateAuthchallengeName != null
                            && initiateAuthchallengeName.trim().equals("SOFTWARE_TOKEN_MFA")) {
                        System.out.println("Enter the MFA TOKEN = ");
                        //System.out.println(" ");
                        //String mfaToken = scanner.nextLine();
                        String mfaToken = vo.getMfaToken();
                        String authMFATokenRespStr = sendPostRespondToAuthMFATOKEN(sessionId, mfaToken);
                        //System.out.println("authMFATokenRespStr: " + authMFATokenRespStr);
                        JsonNode authMFATokenResponse = objectMapper.readValue(authMFATokenRespStr, JsonNode.class);
                        if (authMFATokenResponse != null && authMFATokenResponse instanceof NullNode == false) {
                            JsonNode authResultNode = authMFATokenResponse.get("AuthenticationResult");
                            if (authResultNode != null) {
                                JsonNode refreshTokenNode = authResultNode.get("RefreshToken");
                                if (refreshTokenNode != null) {
                                    refreshToken = refreshTokenNode.asText();
                                    if (refreshToken != null) {
                                        System.out.println("Refresh Token :" + refreshToken);
                                        //CognitoJWTParser.ValidateToken(refreshToken);
                                        System.out.println("");
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return refreshToken;
    }

    public String getIDToken(CognitoVO vo) {
        String refreshToken = vo.getRefreshToken();
        String idToken = "";
        try {
            if (refreshToken == null || refreshToken.trim().equals("")) {
                refreshToken = getRefreshToken(vo);
            }

            String initiateAuthResponseStr = sendPostInitiateAuthForIdToken(refreshToken);
            if (initiateAuthResponseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode initiateAuthResponse = objectMapper.readValue(initiateAuthResponseStr, JsonNode.class);
                if (initiateAuthResponse != null && initiateAuthResponse instanceof NullNode == false) {
                    JsonNode authResultNode = initiateAuthResponse.get("AuthenticationResult");
                    if (authResultNode != null) {
                        JsonNode refreshTokenNode = authResultNode.get("IdToken");
                        if (refreshTokenNode != null) {
                            idToken = refreshTokenNode.asText();
                            if (idToken != null) {
                                System.out.println("Id Token :" + idToken);
                                //CognitoJWTParser.ValidateToken(idToken,cognitoPoolPublicKeyMap);
                                System.out.println("");
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idToken;
    }

    public String getAccessToken(CognitoVO vo ) {
        String refreshToken = vo.getRefreshToken();
        String accessToken = "";
        try {
            if (refreshToken == null || refreshToken.trim().equals("")) {
                refreshToken = getRefreshToken(vo);
            }

            String initiateAuthResponseStr = sendPostInitiateAuthForIdToken(refreshToken);
            if (initiateAuthResponseStr != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode initiateAuthResponse = objectMapper.readValue(initiateAuthResponseStr, JsonNode.class);
                if (initiateAuthResponse != null && initiateAuthResponse instanceof NullNode == false) {
                    JsonNode authResultNode = initiateAuthResponse.get("AuthenticationResult");
                    if (authResultNode != null) {
                        JsonNode refreshTokenNode = authResultNode.get("AccessToken");
                        if (refreshTokenNode != null) {
                            accessToken = refreshTokenNode.asText();
                            if (accessToken != null) {
                                System.out.println("Access Token :" + accessToken);
                                //CognitoJWTParser.ValidateToken(accessToken,cognitoPoolPublicKeyMap);
                                System.out.println("");
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accessToken;
    }

    public void downloadCaseData(CognitoVO vo) {
        String idToken = vo.getIdToken();
        try {
            //if (idToken == null || idToken.trim().equals(""))
            {
                idToken=getIDToken(vo);
            }
            sendGetCaseDataRequest(idToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadCaseDocument(CognitoVO vo) {
        String idToken = vo.getIdToken();
        try {
            //if (idToken == null || idToken.trim().equals(""))
            {
                idToken=getIDToken(vo);
            }
            sendGetCaseDocumentRequest(idToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void readConfig() {
            System.out.println("app_client_id: " + app_client_id);
            System.out.println("User : " + userName);
            System.out.println("User Pwd : " + userPwd);
            System.out.println("cognito_url: " + cognito_url);
            System.out.println("API Gateway: " + awsGatewayApi);
            System.out.println("API Gateway Doc : " + awsGatewayDocApi);
            System.out.println("Cognito Pool Public Key URL: " + cognitoPoolPublicKeyUrl);

    }

    private String sendPostInitiateAuthCognito() throws Exception {
        String cognitoResponseStr = "";
        HttpPost post = new HttpPost(cognito_url);

        post.setHeader("Content-Type", "application/x-amz-json-1.1");
        post.setHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth");
        post.setHeader("Accept", "application/json");

        String payload = "{\"AuthParameters\": {" + "\"USERNAME\":\"" + userName + "\"," + "\"PASSWORD\":\"" + userPwd
                + "\"" + "}," + "\"AuthFlow\": \"USER_PASSWORD_AUTH\"," + "\"ClientId\": \"" + app_client_id + "\"" +
                "}";

        System.out.println(" ----------------- sendPostInitiateAuthCognito ------------");
        System.out.println(payload);
        System.out.println(" ---------------------------------------------------------- ");

        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(post)) {
            cognitoResponseStr = EntityUtils.toString(response.getEntity());
        }
        return cognitoResponseStr;
    }

    private String sendPostAssociateSoftwareToken(String sessionId) throws Exception {
        String associateSoftwareTokenStr = "";

        HttpPost post = new HttpPost(cognito_url);

        post.setHeader("Content-Type", "application/x-amz-json-1.1");
        post.setHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.AssociateSoftwareToken");
        post.setHeader("Accept", "application/json");

        String payload = "{" + "\"Session\": \"" + sessionId + "\"" + "}";

        System.out.println(" ----------------- sendPostAssociateSoftwareToken ----------");
        System.out.println(payload);
        System.out.println(" ---------------------------------------------------------- ");

        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(post)) {
            associateSoftwareTokenStr = EntityUtils.toString(response.getEntity());
        }
        return associateSoftwareTokenStr;
    }

    public String sendPostVerifySoftwareToken(String secretCode, String sessionId) throws Exception {
        String verifySoftwareTokenStr = "";
        HttpPost post = new HttpPost(cognito_url);

        post.setHeader("Content-Type", "application/x-amz-json-1.1");
        post.setHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.VerifySoftwareToken");
        post.setHeader("Accept", "application/json");

        String payload = "{" + "\"Session\": \"" + sessionId + "\"," + "\"UserCode\":\"" + secretCode + "\"" +

                "}";

        System.out.println(" ----------------- sendPostVerifySoftwareToken ------------- ");
        System.out.println(payload);
        System.out.println(" ----------------------------------------------------------- ");

        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(post)) {
            verifySoftwareTokenStr = EntityUtils.toString(response.getEntity());
        }

        return verifySoftwareTokenStr;
    }

    public String sendPostRespondToAuthMFATOKEN(String sessionId, String mfaToken) throws Exception {
        String softwareTokenMfaStr = "";
        HttpPost post = new HttpPost(cognito_url);

        post.setHeader("Content-Type", "application/x-amz-json-1.1");
        post.setHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.RespondToAuthChallenge");
        post.setHeader("Accept", "application/json");

        String payload = "{" + "\"ChallengeName\": \"SOFTWARE_TOKEN_MFA\"," + "\"ChallengeResponses\":{"
                + "\"SOFTWARE_TOKEN_MFA_CODE\": \"" + mfaToken + "\"," + "\"USERNAME\":\"" + userName + "\"" + "},"
                + "\"ClientId\": \"" + app_client_id + "\"," + "\"Session\": \"" + sessionId + "\"" + "}";

        /*
         * "ChallengeName": "SOFTWARE_TOKEN_MFA", "ChallengeResponses":{
         * "SOFTWARE_TOKEN_MFA_CODE": "", "USERNAME": "anagappan" }, "ClientId":
         * "43j7mve9qcocrpdffskhheefjv", "Session": ""
         */
        System.out.println(" ----------------- sendPostRespondToAuthMFATOKEN ------------- ");
        System.out.println(payload);
        System.out.println(" ----------------------------------------------------------- ");

        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(post)) {
            softwareTokenMfaStr = EntityUtils.toString(response.getEntity());
        }

        return softwareTokenMfaStr;
    }

    private String sendPostInitiateAuthForIdToken(String refreshToken) throws Exception {
        String cognitoResponseStr = "";

        HttpPost post = new HttpPost(cognito_url);

        post.setHeader("Content-Type", "application/x-amz-json-1.1");
        post.setHeader("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth");
        post.setHeader("Accept", "application/json");

        String payload = "{\"AuthParameters\": {" + "\"REFRESH_TOKEN\":\"" + refreshToken + "\"" + "},"
                + "\"AuthFlow\": \"REFRESH_TOKEN_AUTH\"," + "\"ClientId\": \"" + app_client_id + "\"" +

                "}";
        /*
         * { "AuthParameters": { "REFRESH_TOKEN": "" }, "AuthFlow":
         * "REFRESH_TOKEN_AUTH", "ClientId": "43j7mve9qcocrpdffskhheefjv"
         *
         * }
         */
        System.out.println(" ----------------- sendPostInitiateAuthForIdToken ------------");
        System.out.println(payload);
        System.out.println(" ---------------------------------------------------------- ");

        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(post)) {
            cognitoResponseStr = EntityUtils.toString(response.getEntity());
        }
        return cognitoResponseStr;
    }

    private String sendGetCaseDataRequest(String idToken) throws Exception {

        String jsonData = "";

        URIBuilder builder = new URIBuilder(awsGatewayApi);
        builder.setParameter("TableName", "taxcases");
        URI reqUri = builder.build();
        //System.out.println("URI: " +reqUri.getPath());
        //System.out.println("idToken #" +idToken+"#");

        HttpGet request = new HttpGet(reqUri);

        // add request headers
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", idToken);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(request)){

            // Get HttpResponse Status
            //System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                jsonData = EntityUtils.toString(entity);
                System.out.println(jsonData);
            }

        }
        return jsonData;
    }

    private String sendGetCognitoPoolPublicKeyRequest() throws Exception {

        String jsonData = "";

        URIBuilder builder = new URIBuilder(cognitoPoolPublicKeyUrl);
        //builder.setParameter("TableName", "taxcases");
        URI reqUri = builder.build();
        //System.out.println("URI: " +reqUri.getPath());

        HttpGet request = new HttpGet(reqUri);

        // add request headers
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        //request.addHeader("Authorization", idToken);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            //System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                jsonData = EntityUtils.toString(entity);
                System.out.println("Public Key : "+jsonData);
            }

        }
        return jsonData;
    }

    private String sendGetCaseDocumentRequest(String idToken) throws Exception {

        String documentStr = "";

        URIBuilder builder = new URIBuilder(awsGatewayDocApi);
        //builder.setParameter("TableName", "taxcases");
        URI reqUri = builder.build();
        //System.out.println("URI: " +reqUri.getPath());
        //System.out.println("idToken #" +idToken+"#");

        HttpGet request = new HttpGet(reqUri);

        // add request headers
        //request.addHeader("Content-Type", "application/pdf");
        //request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", idToken);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();

             CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            //System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                // return it as a String
                documentStr = EntityUtils.toString(entity);
                System.out.println("Document Content: " +documentStr);
            }

        }
        return documentStr;
    }
}
