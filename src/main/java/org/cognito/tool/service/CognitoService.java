package org.cognito.tool.service;

import org.cognito.tool.vo.CognitoVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CognitoService {

    public List<Map<String, Object>> getChoices();

    Object processChoice(CognitoVO vo);

    byte[] downloadDocument(CognitoVO vo) throws IOException;

	public Object submotmfs(CognitoVO vo);

	public String getscCode();
}
