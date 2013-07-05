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
 * Monitor for CALL events.
 * 
 * @author Fabio D. C. Depin
 */
public class CallConnectorListener extends AbstractConnectorListener {
    
    private CallMonitorListener callMonitorListener = null;
    
    CallConnectorListener(CallMonitorListener listener) {
        callMonitorListener = listener;
    }
    
    @Override
    public void messageReceived(ConnectorMessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("CALL ")) {
            String data = message.substring("CALL ".length());
            String id = data.substring(0, data.indexOf(' '));
            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
            String propertyName = propertyNameAndValue.substring(0,
                    propertyNameAndValue.indexOf(' '));
            if ("STATUS".equals(propertyName)) {
                String propertyValue = propertyNameAndValue
                        .substring(propertyNameAndValue.indexOf(' ') + 1);
                Call.Status status = Call.Status.valueOf(propertyValue);
                if (callMonitorListener != null){
                    Call call = Call.getInstance(id, callMonitorListener);
                    call.fireCallMonitor(status);
                } else {
                    Call call = Call.getInstance(id);
                }
                EXIT:
                if (status == Call.Status.FINISHED || status == Call.Status.CANCELLED || status == Call.Status.FAILED){
                    break EXIT;
                }
            }
        }
    }    
}
