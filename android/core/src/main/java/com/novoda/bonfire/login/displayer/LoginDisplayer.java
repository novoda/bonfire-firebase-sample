package com.novoda.bonfire.login.displayer;

public interface LoginDisplayer {

    void attach(LoginActionListener actionListener);

    void detach(LoginActionListener actionListener);

    void showAuthenticationError(String message);

    interface LoginActionListener {

        void onGooglePlusLoginSelected();

    }

}
