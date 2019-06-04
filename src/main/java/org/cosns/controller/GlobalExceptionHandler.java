package org.cosns.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException;
import org.cosns.util.ConstantsUtil;
import org.cosns.util.DefaultException;
import org.cosns.web.result.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
	public final Logger logger = LoggerFactory.getLogger(getClass());

	@ExceptionHandler({ DefaultException.class })
	@ResponseBody
	public ErrorResult jsonErrorHandler(HttpServletRequest req, DefaultException e) throws Exception {
		ErrorResult r = new ErrorResult();
		r.setRemarks(e.getLocalizedMessage());
		r.setErrorURL(req.getRequestURL().toString());
		r.setStatus(ConstantsUtil.RESULT_ERROR);
		return r;
	}

	@ExceptionHandler({ SizeLimitExceededException.class })
	@ResponseBody
	public ErrorResult sizeLimitExceededException(HttpServletRequest req, SizeLimitExceededException e) throws Exception {
		ErrorResult r = new ErrorResult();
		r.setRemarks(e.getLocalizedMessage());
		r.setErrorURL(req.getRequestURL().toString());
		r.setStatus(ConstantsUtil.RESULT_ERROR);
		return r;
	}

}