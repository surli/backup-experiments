/*
 * Copyright (c) 2012-2016 Biznet, Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.page.forgetpassword;

import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.prism.query.builder.QueryBuilder;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.extensions.validation.validator.RfcCompliantEmailAddressValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import com.evolveum.midpoint.common.policy.ValuePolicyGenerator;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.match.PolyStringOrigMatchingRule;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.error.PageError;
import com.evolveum.midpoint.web.page.forgetpassword.ResetPolicyDto.ResetMethod;
import com.evolveum.midpoint.web.page.login.PageLogin;
import com.evolveum.midpoint.web.page.login.PageRegistrationBase;
import com.evolveum.midpoint.web.page.login.PageSelfRegistration;
import com.evolveum.midpoint.web.page.login.SelfRegistrationDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsResetMethodType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsResetTypeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NonceCredentialsPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NonceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordCredentialsPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SecurityPolicyType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ServiceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

@PageDescriptor(url = "/forgotpassword")
public class PageForgotPassword extends PageRegistrationBase {
	private static final long serialVersionUID = 1L;

	private static final String ID_PWDRESETFORM = "pwdresetform";
	private static final String ID_USERNAME_CONTAINER = "usernameContainer";
	private static final String ID_USERNAME = "username";
	private static final String ID_EMAIL_CONTAINER = "emailContainer";
	private static final String ID_EMAIL = "email";
	private static final String ID_SUBMIT = "submitButton";
	private static final String ID_BACK = "back";

	private static final String DOT_CLASS = PageForgotPassword.class.getName() + ".";
	protected static final String OPERATION_LOAD_RESET_PASSWORD_POLICY = DOT_CLASS
			+ "loadPasswordResetPolicy";
	private static final String ID_PASSWORD_RESET_SUBMITED = "resetPasswordInfo";
	private static final String OPERATION_LOAD_USER = DOT_CLASS + "loadUser";

	private static final Trace LOGGER = TraceManager.getTrace(PageForgotPassword.class);

	public PageForgotPassword() {
		super();
		initLayout();
	}

	private boolean submited;

	@Override
	protected void createBreadcrumb() {
		// don't create breadcrumb for this page
	}

	private void initLayout() {
		Form form = new Form(ID_PWDRESETFORM);
		form.setOutputMarkupId(true);
		form.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !submited;
			}

		});

		WebMarkupContainer userNameContainer = new WebMarkupContainer(ID_USERNAME_CONTAINER);
		userNameContainer.setOutputMarkupId(true);
		form.add(userNameContainer);
		
		RequiredTextField<String> userName = new RequiredTextField<String>(ID_USERNAME, new Model<String>());
		userName.setOutputMarkupId(true);
		userNameContainer.add(userName);
		userNameContainer.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				return getResetPasswordPolicy().getResetMethod() == ResetMethod.SECURITY_QUESTIONS;
			};
		});

		
		WebMarkupContainer emailContainer = new WebMarkupContainer(ID_EMAIL_CONTAINER);
		emailContainer.setOutputMarkupId(true);
		form.add(emailContainer);
		RequiredTextField<String> email = new RequiredTextField<String>(ID_EMAIL, new Model<String>());
		email.add(RfcCompliantEmailAddressValidator.getInstance());
		email.setOutputMarkupId(true);
		emailContainer.add(email);
		emailContainer.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			public boolean isVisible() {
				ResetMethod resetMethod = getResetPasswordPolicy().getResetMethod();
				return  resetMethod == ResetMethod.SECURITY_QUESTIONS
						|| resetMethod == ResetMethod.MAIL;
			};
		});

		AjaxSubmitButton submit = new AjaxSubmitButton(ID_SUBMIT) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				processResetPassword(target, form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(getFeedbackPanel());
			}

		};
		submit.setOutputMarkupId(true);
		form.add(submit);
		
		AjaxButton backButton = new AjaxButton(ID_BACK) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				setResponsePage(PageLogin.class);
			}
		};
		backButton.setOutputMarkupId(true);
		form.add(backButton);

		add(form);
		
		MultiLineLabel label = new MultiLineLabel(ID_PASSWORD_RESET_SUBMITED,
				createStringResource("PageForgotPassword.form.submited.message"));
		add(label);
		label.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return submited;
			}

			@Override
			public boolean isEnabled() {
				return submited;
			}

		});

	}
	
	private void processResetPassword(AjaxRequestTarget target, Form<?> form) {
		
			RequiredTextField<String> username = (RequiredTextField) form.get(createComponentPath(ID_USERNAME_CONTAINER, ID_USERNAME));
			RequiredTextField<String> email = (RequiredTextField) form.get(createComponentPath(ID_EMAIL_CONTAINER, ID_EMAIL));
			String usernameValue = username != null ? username.getModelObject() : null;
			String emailValue = email != null ? email.getModelObject() : null;
			LOGGER.debug("Reset Password user info form submitted. username={}, email={}",
					usernameValue, emailValue);

			final UserType user = checkUser(emailValue, usernameValue);
			LOGGER.trace("Reset Password user: {}", user);

			
			if (user == null) {
				LOGGER.debug("User for username={}, email={} not found", usernameValue,
						emailValue);
				getSession().error(getString("pageForgetPassword.message.usernotfound"));
				throw new RestartResponseException(PageForgotPassword.class);
			}
//			try {
			
			if (getResetPasswordPolicy() == null) {
				LOGGER.debug("No policies for reset password defined");
				getSession().error(getString("pageForgetPassword.message.policy.not.found"));
				throw new RestartResponseException(PageForgotPassword.class);
			}
		
			switch (getResetPasswordPolicy().getResetMethod()) {
					case MAIL:
						OperationResult result = saveUserNonce(user,
								getResetPasswordPolicy().getNoncePolicy());
						if (result.getStatus() == OperationResultStatus.SUCCESS) {
							submited = true;
							target.add(PageForgotPassword.this);
						} else {
							getSession().error(getString("PageForgotPassword.send.nonce.failed"));
							throw new RestartResponseException(PageForgotPassword.this);
						}

						break;
					case SECURITY_QUESTIONS:
						getSession().setAttribute("pOid", user.getOid());
						LOGGER.trace("Forward to PageSecurityQuestions");
						setResponsePage(PageSecurityQuestions.class);
						break;
					default:
						getSession().error(
								getString("pageForgetPassword.message.reset.method.not.supported"));
						throw new RestartResponseException(PageForgotPassword.this);
				}


	
//		} catch (Throwable e) {
//			LOGGER.error("Error during processing of security questions: {}", e.getMessage(), e);
//			// Just log the error, but do not display it. We are still
//			// in unprivileged part of the web
//			// we do not want to provide any information to the
//			// attacker.
//			throw new RestartResponseException(PageError.class);
//		}

	}

	private OperationResult saveUserNonce(final UserType user, final NonceCredentialsPolicyType noncePolicy) {
		return runPrivileged(new Producer<OperationResult>() {

			@Override
			public OperationResult run() {
				Task task = createAnonymousTask("generateUserNonce");
				task.setChannel(SchemaConstants.CHANNEL_GUI_RESET_PASSWORD_URI);
				task.setOwner(user.asPrismObject());
				OperationResult result = new OperationResult("generateUserNonce");
				ProtectedStringType nonceCredentials = new ProtectedStringType();
				nonceCredentials.setClearValue(generateNonce(noncePolicy, task, result));

				NonceType nonceType = new NonceType();
				nonceType.setValue(nonceCredentials);

				ObjectDelta<UserType> nonceDelta;
				try {
					nonceDelta = ObjectDelta.createModificationReplaceContainer(UserType.class, user.getOid(),
							SchemaConstants.PATH_NONCE, getPrismContext(), nonceType);

					WebModelServiceUtils.save(nonceDelta, result, task, PageForgotPassword.this);
				} catch (SchemaException e) {
					result.recordFatalError("Failed to generate nonce for user");
					LoggingUtils.logException(LOGGER, "Failed to generate nonce for user: " + e.getMessage(),
							e);
				}

				result.computeStatusIfUnknown();
				return result;
			}

		});
	}

	private String generateNonce(NonceCredentialsPolicyType noncePolicy, Task task, OperationResult result) {
		ValuePolicyType policy = null;

		if (noncePolicy != null && noncePolicy.getValuePolicyRef() != null) {
			PrismObject<ValuePolicyType> valuePolicy = WebModelServiceUtils.loadObject(ValuePolicyType.class,
					noncePolicy.getValuePolicyRef().getOid(), PageForgotPassword.this, task, result);
			policy = valuePolicy.asObjectable();
		}

		return ValuePolicyGenerator.generate(policy != null ? policy.getStringPolicy() : null, 24, result);
	}

	// Check if the user exists with the given email and username in the idm
	public UserType checkUser(final String email, final String username) {

		UserType user = runPrivileged(new Producer<UserType>() {

			@Override
			public UserType run() {
				return getUser(email, username);
			}

			@Override
			public String toString() {
				return DOT_CLASS + "getUser";
			}

		});

		LOGGER.trace("got user {}", user);
		if (user == null) {
			return null;
		}

		if (user.getEmailAddress().equalsIgnoreCase(email)) {
			return user;
		} else {
			LOGGER.debug(
					"The supplied e-mail address '{}' and the e-mail address of user {} '{}' do not match",
					email, user, user.getEmailAddress());
			return null;
		}

	}

	private UserType getUser(String email, String username) {
		try {

			Task task = createAnonymousTask(OPERATION_LOAD_USER);
			OperationResult result = task.getResult();

			ObjectQuery query = null;

			switch (getResetPasswordPolicy().getResetMethod()) {
				case MAIL:
					query = QueryBuilder.queryFor(UserType.class, getPrismContext())
							.item(UserType.F_EMAIL_ADDRESS).eq(email).matchingCaseIgnore().build();
					break;
				case SECURITY_QUESTIONS:
					query = QueryBuilder.queryFor(UserType.class, getPrismContext()).item(UserType.F_NAME)
							.eqPoly(username).matchingNorm().and().item(UserType.F_EMAIL_ADDRESS).eq(email)
							.matchingCaseIgnore().build();
					break;
				default:
					getSession().error(getString("PageForgotPassword.unsupported.reset.type"));
					throw new RestartResponseException(PageForgotPassword.this);
			}

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Searching for user with query:\n{}", query.debugDump(1));
			}

			Collection<SelectorOptions<GetOperationOptions>> options = SelectorOptions
					.createCollection(GetOperationOptions.createNoFetch());
			// Do NOT use WebModelServiceUtils.searchObjects() here. We do NOT
			// want the standard error handling.
			List<PrismObject<UserType>> userList = ((PageBase) getPage()).getModelService()
					.searchObjects(UserType.class, query, options, task, result);

			if ((userList == null) || (userList.isEmpty())) {
				LOGGER.trace("Empty user list in ForgetPassword");
				return null;
			}

			UserType user = userList.get(0).asObjectable();
			LOGGER.trace("User found for ForgetPassword: {}", user);

			return user;

		} catch (Exception e) {
			LOGGER.error("Error getting user: {}", e.getMessage(), e);
			// Just log the error, but do not display it. We are still in
			// unprivileged part of the web
			// we do not want to provide any information to the attacker.
			return null;
		}

	}

}
