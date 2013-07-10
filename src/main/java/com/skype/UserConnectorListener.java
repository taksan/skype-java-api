/*******************************************************************************
 * Copyright 2013 Fabio D. C. Depin <fabiodepin@gmail.com>.
 *
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
 * 
 * Contributors:
 * Fabio D. C. Depin <fabiodepin@gmail.com> - initial implementation this Class
 ******************************************************************************/
package com.skype;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * Monitor for USER events.
 * 
 * @author Fabio D. C. Depin
 */
public class UserConnectorListener extends AbstractConnectorListener {
    private UserListener userListener = null;
    
    UserConnectorListener(UserListener listener) {
        userListener = listener;
    }
    
    @Override
    public void messageReceived(ConnectorMessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("USER ")) {
            String data = message.substring("USER ".length());
            String id = data.substring(0, data.indexOf(' '));
            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
            String propertyName = propertyNameAndValue.substring(0,
                    propertyNameAndValue.indexOf(' '));
            String propertyValue = propertyNameAndValue
                    .substring(propertyNameAndValue.indexOf(' ') + 1);
            if (userListener != null) {
                User user = User.getInstance(id, userListener);
                if (propertyName.equals("ONLINESTATUS")){
                    user.fireStatusMonitor(User.Status.valueOf(propertyValue));
                } else if (propertyName.equals("MOOD_TEXT")){
                    user.fireMoodTextMonitor(propertyValue);
                } else if (propertyName.equals("FULLNAME")){
                    user.fireFullNameMonitor(propertyValue);
                } else if (propertyName.equals("PHONE_MOBILE")){
                    user.firePhoneMobileMonitor(propertyValue);
                } else if (propertyName.equals("PHONE_HOME")){
                    user.firePhoneHomeMonitor(propertyValue);
                } else if (propertyName.equals("PHONE_OFFICE")){
                    user.firePhoneOfficeMonitor(propertyValue);
                } else if (propertyName.equals("DISPLAYNAME")){
                    user.fireDisplayNameMonitor(propertyValue);
                } else if (propertyName.equals("COUNTRY")){
                    user.fireCountryMonitor(propertyValue);
                } else if (propertyName.equals("PROVINCE")){
                    user.fireProvinceMonitor(propertyValue);
                } else if (propertyName.equals("CITY")){
                    user.fireCityMonitor(propertyValue);
                } else if (propertyName.equals("TIMEZONE")){
                    user.fireTimeZoneMonitor(propertyValue);
                } else if (propertyName.equals("SEX")){
                    user.fireSexMonitor(User.Sex.valueOf(propertyValue));
                } else if (propertyName.equals("HOMEPAGE")){
                    user.fireHomePageMonitor(propertyValue);
                } else if (propertyName.equals("BIRTHDAY")){
                    user.fireBirthdayMonitor(propertyValue);
                } else if (propertyName.equals("LANGUAGE")){
                    user.fireLanguageMonitor(propertyValue);
                } else if (propertyName.equals("ABOUT")){
                    user.fireAboutMonitor(propertyValue);
                } else if (propertyName.equals("ISBLOCKED")){
                    user.fireIsBlockedMonitor(Boolean.valueOf(propertyValue));
                } else if (propertyName.equals("ISAUTHORIZED")){
                    user.fireIsAuthorizedMonitor(Boolean.valueOf(propertyValue));
                } 
            } else {
                User user = User.getInstance(id);
            }
        }
    }
}
