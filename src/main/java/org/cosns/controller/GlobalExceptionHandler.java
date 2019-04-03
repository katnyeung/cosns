package org.cosns.controller;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.cosns.util.ConstantsUtil;
import org.cosns.util.DefaultException;
import org.cosns.web.result.ErrorResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({ DefaultException.class, ParseException.class })
	@ResponseBody
	public ErrorResult jsonErrorHandler(HttpServletRequest req, DefaultException e) throws Exception {
		ErrorResult r = new ErrorResult();
		r.setErrorRemark(e.getLocalizedMessage());
		r.setErrorURL(req.getRequestURL().toString());
		r.setStatus(ConstantsUtil.RESULT_ERROR);
		return r;
	}

}