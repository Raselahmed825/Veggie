package com.greentopli.core.presenter.signup;

import android.content.Context;
import android.support.annotation.NonNull;

import com.greentopli.core.presenter.base.BasePresenter;
import com.greentopli.core.remote.BackendConnectionService;
import com.greentopli.core.remote.ServiceGenerator;
import com.greentopli.core.storage.helper.UserDbHelper;
import com.greentopli.model.BackendResult;
import com.greentopli.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by rnztx on 23/10/16.
 */

public class UserSignUpPresenter extends BasePresenter<SignUpView> {
	private UserDbHelper userDbHelper;
	private Call<BackendResult> signUpCall;

	public static UserSignUpPresenter bind(SignUpView view, Context context) {
		UserSignUpPresenter presenter = new UserSignUpPresenter();
		presenter.attachView(view, context);
		return presenter;
	}

	@Override
	public void attachView(SignUpView mvpView, Context context) {
		super.attachView(mvpView, context);
		userDbHelper = new UserDbHelper(context);
	}

	public void signUp(@NonNull final User user) {
		BackendConnectionService service = ServiceGenerator.createService(BackendConnectionService.class);
		signUpCall = service.signUpUser(user);
		signUpCall.enqueue(new Callback<BackendResult>() {
			@Override
			public void onResponse(Call<BackendResult> call, Response<BackendResult> response) {
				// Stored on server
				if (response.body() != null && response.body().isResult()) {
					// now store locally
					if (userDbHelper.storeUserInfo(user) <= 0)
						getmMvpView().onSignUpError("Failed to store Locally");
					else
						getmMvpView().onSignUpSuccess();
				} else { // Failed to store on server
					getmMvpView().onSignUpError("Error uploading data");
				}

				getmMvpView().showProgressbar(false);
			}

			@Override
			public void onFailure(Call<BackendResult> call, Throwable t) {
				getmMvpView().onSignUpError(" Connection Error " + t.getMessage());
				getmMvpView().showProgressbar(false);
			}
		});
	}

	public void updateInstanceId(String instanceId) {
		User user = userDbHelper.getSignedUserInfo();
		if (user != null && !user.getInstanceId().equals(instanceId)) {
			user.setInstanceId(instanceId);
			// update on server
			signUp(user);
		} else {
			getmMvpView().onSignUpError("Not Updating Instance Id");
		}
	}
}
