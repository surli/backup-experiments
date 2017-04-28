package org.ff4j.web.controller;

/*
 * #%L
 * ff4j-sample-web
 * %%
 * Copyright (C) 2013 - 2016 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ff4j.FF4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

/**
 * Redirecto to 404 page
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class NotFoundController extends AbstractController {
	
	/** View name. */
	private static final String VIEW_NOTFOUND = "404";
	
	/** {@inheritDoc} */
	public NotFoundController(FF4j ff4j, TemplateEngine te) {
		super(ff4j, VIEW_NOTFOUND, te);
	}

	/** {@inheritDoc} */
    public void post(HttpServletRequest req, HttpServletResponse res, WebContext ctx)
    throws IOException {
    }
    
    /** {@inheritDoc} */
    public void get(HttpServletRequest req, HttpServletResponse res, WebContext ctx)
	throws IOException {
		ctx.setVariable(KEY_TITLE, "Not Found ");
	}


}
