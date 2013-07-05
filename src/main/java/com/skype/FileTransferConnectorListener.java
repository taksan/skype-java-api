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
 * Monitor for FILETRANSFER events.
 * 
 * @author Fabio D. C. Depin
 */
final class FileTransferConnectorListener extends AbstractConnectorListener {

    private FileTransferListener fileTransferListener = null;
    
    FileTransferConnectorListener(FileTransferListener listener) {
        fileTransferListener = listener;
    }

    @Override
    public void messageReceived(ConnectorMessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith("FILETRANSFER ")) {
            String data = message.substring("FILETRANSFER ".length());
            String id = data.substring(0, data.indexOf(' '));
            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
            String propertyName = propertyNameAndValue.substring(0,
                    propertyNameAndValue.indexOf(' '));
            if ("STATUS".equals(propertyName)) {
                String propertyValue = propertyNameAndValue
                        .substring(propertyNameAndValue.indexOf(' ') + 1);
                FileTransfer.Status status = FileTransfer.Status.valueOf(propertyValue);
                if (fileTransferListener != null){
                    FileTransfer fileTransfer = FileTransfer.getInstance(id, fileTransferListener);
                    fileTransfer.fireFileTransfer(status);
                } else {
                    FileTransfer fileTransfer = FileTransfer.getInstance(id);
                }
                EXIT:
                if (status == FileTransfer.Status.COMPLETED || status == FileTransfer.Status.CANCELLED || status == FileTransfer.Status.FAILED){
                    break EXIT;
                }
            }
        }
    }
}