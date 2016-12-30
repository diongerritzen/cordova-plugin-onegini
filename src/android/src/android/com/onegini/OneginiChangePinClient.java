/*
 * Copyright (c) 2016 Onegini B.V.
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

package com.onegini;

import static com.onegini.OneginiCordovaPluginConstants.ERROR_CODE_CREATE_PIN_NO_REGISTRATION_IN_PROGRESS;
import static com.onegini.OneginiCordovaPluginConstants.ERROR_CODE_OPERATION_CANCELED;
import static com.onegini.OneginiCordovaPluginConstants.ERROR_CODE_PROVIDE_PIN_NO_AUTHENTICATION_IN_PROGRESS;
import static com.onegini.OneginiCordovaPluginConstants.ERROR_DESCRIPTION_CREATE_PIN_NO_REGISTRATION_IN_PROGRESS;
import static com.onegini.OneginiCordovaPluginConstants.ERROR_DESCRIPTION_OPERATION_CANCELED;
import static com.onegini.OneginiCordovaPluginConstants.ERROR_DESCRIPTION_PROVIDE_PIN_NO_AUTHENTICATION_IN_PROGRESS;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import com.onegini.handler.ChangePinHandler;
import com.onegini.handler.CreatePinRequestHandler;
import com.onegini.handler.PinAuthenticationRequestHandler;
import com.onegini.mobile.sdk.android.client.OneginiClient;
import com.onegini.mobile.sdk.android.handlers.request.callback.OneginiPinCallback;
import com.onegini.util.ActionArgumentsUtil;
import com.onegini.util.PluginResultBuilder;

public class OneginiChangePinClient extends CordovaPlugin {

  private final static String ACTION_START = "start";
  private final static String ACTION_CREATE_PIN = "createPin";
  private final static String ACTION_PROVIDE_PIN = "providePin";
  private static final String ACTION_CANCEL_FLOW = "cancelFlow";

  @Override
  public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (ACTION_START.equals(action)) {
      startChangePin(callbackContext);
      return true;
    } else if (ACTION_CREATE_PIN.equals(action)) {
      createPin(args, callbackContext);
      return true;
    } else if (ACTION_PROVIDE_PIN.equals(action)) {
      providePin(args, callbackContext);
      return true;
    } else if (ACTION_CANCEL_FLOW.equals(action)) {
      cancelFlow(callbackContext);
      return true;
    }

    return false;
  }

  private void startChangePin(final CallbackContext callbackContext) throws JSONException {
    PinAuthenticationRequestHandler.getInstance().setStartAuthenticationCallbackContext(callbackContext);
    CreatePinRequestHandler.getInstance().setOnStartPinCreationCallback(callbackContext);

    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        final ChangePinHandler changePinHandler = new ChangePinHandler(callbackContext);
        getOneginiClient().getUserClient().changePin(changePinHandler);
      }
    });
  }

  private void providePin(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    final String pin = ActionArgumentsUtil.getPinFromArguments(args);
    final OneginiPinCallback pinCallback = PinAuthenticationRequestHandler.getInstance().getPinCallback();

    if (pinCallback == null) {
      callbackContext.sendPluginResult(new PluginResultBuilder()
          .withPluginError(ERROR_DESCRIPTION_PROVIDE_PIN_NO_AUTHENTICATION_IN_PROGRESS, ERROR_CODE_PROVIDE_PIN_NO_AUTHENTICATION_IN_PROGRESS)
          .build());

      return;
    }

    pinCallback.acceptAuthenticationRequest(pin.toCharArray());
  }

  private void createPin(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
    final String pin = ActionArgumentsUtil.getPinFromArguments(args);
    final OneginiPinCallback pinCallback = CreatePinRequestHandler.getInstance().getOneginiPinCallback();

    if (pinCallback == null) {
      final PluginResult pluginResult = new PluginResultBuilder()
          .withError()
          .withPluginError(ERROR_DESCRIPTION_CREATE_PIN_NO_REGISTRATION_IN_PROGRESS, ERROR_CODE_CREATE_PIN_NO_REGISTRATION_IN_PROGRESS)
          .build();
      callbackContext.sendPluginResult(pluginResult);
    } else {
      pinCallback.acceptAuthenticationRequest(pin.toCharArray());
    }

  }

  private void cancelFlow(final CallbackContext callbackContext) {
    callbackContext.sendPluginResult(new PluginResultBuilder()
        .withPluginError(ERROR_DESCRIPTION_OPERATION_CANCELED, ERROR_CODE_OPERATION_CANCELED)
        .build());
  }

  private OneginiClient getOneginiClient() {
    return OneginiSDK.getInstance().getOneginiClient(cordova.getActivity().getApplicationContext());
  }
}