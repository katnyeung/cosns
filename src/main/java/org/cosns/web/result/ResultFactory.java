package org.cosns.web.result;

import org.cosns.util.ConstantsUtil;

public class ResultFactory {
	public static DefaultResult getDefault(String status, String remarks) {
		DefaultResult dr = new DefaultResult();
		dr.setStatus(status);
		dr.setRemarks(remarks);
		return dr;
	}

	public static ErrorResult getErrorResult(String status, String remarks) {
		ErrorResult er = new ErrorResult();
		er.setRemarks(ConstantsUtil.ERROR_MESSAGE_LOGIN_REQUIRED);
		er.setStatus(ConstantsUtil.RESULT_ERROR);

		return er;
	}
}