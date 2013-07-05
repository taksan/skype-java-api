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
 * * Contributors:
 * Fabio D. C. Depin <fabiodepin@gmail.com> - initial implementation this Class
 ******************************************************************************/
package com.skype;

/**
 * This is the listener for the FileTransfer object.
 * @see FileTransfer
 * @author Fabio D. C. Depin
 */
public interface FileTransferListener {
    /**
     * Called when the status of a FILETRANSFER object changes.
     * @param FILETRANSFER object.
     * @param STATUS identified that changed.
     * @throws SkypeException when a connection is gone bad.
     */
    void fileTransferMonitor(FileTransfer sentFileTransfer, FileTransfer.Status status) throws SkypeException;
}
