package org.cosns.controller;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.DefaultException;
import org.cosns.web.result.ErrorResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@ExceptionHandler({ DefaultException.class })
	@ResponseBody
	public ErrorResult jsonErrorHandler(HttpServletRequest req, DefaultException e) throws Exception {
		ErrorResult r = new ErrorResult();
		r.setErrorRemark(e.getLocalizedMessage());
		r.setErrorURL(req.getRequestURL().toString());
		r.setStatus(ConstantsUtil.RESULT_ERROR);
		return r;
	}

	@ExceptionHandler({ SizeLimitExceededException.class })
	@ResponseBody
	public ErrorResult sizeLimitExceededException(HttpServletRequest req, SizeLimitExceededException e) throws Exception {
		logger.info("here");
		ErrorResult r = new ErrorResult();
		r.setErrorRemark(e.getLocalizedMessage());
		r.setErrorURL(req.getRequestURL().toString());
		r.setStatus(ConstantsUtil.RESULT_ERROR);
		return r;
	}

}