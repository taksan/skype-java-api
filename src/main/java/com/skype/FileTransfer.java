/*
 * Copyright 2013 Fabio D. C. Depin.
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
 */
package com.skype;

import java.util.HashMap;
import java.util.Map;

/**
 * object representing Skype FILETRANSFER object.
 * File transfer objects are for monitoring purposes only. 
 * No alters/actions via API are currently allowed with these objects. 
 * File transfers cannot be initiated nor accepted via API commands.
 * @see http://dev.skype.com/desktop-api-reference#OBJECT_FILETRANSFER
 * @author Fábio D. C. Depin.
 */
public class FileTransfer extends SkypeObject {
    /**
     * Collection of FileTransfer objects.
     */
    private static final Map<String, FileTransfer> files = new HashMap<String, FileTransfer>();
    
    /**
     * Returns the FileTransfer object by the specified id.
     * @param id whose associated FileTransfer object is to be returned.
     * @return FileTransfer object with ID == id.
     */
    static FileTransfer getInstance(final String id) {
        synchronized (files) {
            if (!files.containsKey(id)) {
                FileTransfer file = new FileTransfer(id);
                files.put(id, file);
            }
            return files.get(id);
        }
    }
    
    /**
     * The <code>Type</code> enum contains the current type of the object.
     */
    public enum Type {
        /**
         * The <code>INCOMING</code> file transfer object from receiving side.
         */
        INCOMING,
        /**
         * The <code>OUTGOING</code> file transfer object from transmitting sidee.
         */
        OUTGOING;
    }
    
    /**
     * The <code>Status</code> enum contains the current status of the object.
     */
    public enum Status {
        /**
         * The <code>NEW</code> initial state of a file transfer. For sender, 
         * the status proceeds to WAITING_FOR_ACCEPT.
         */
        NEW,
        /**
         * The <code>WAITING_FOR_ACCEPT</code> status set for sender until 
         * receiver either accepts or cancels the transfer.
         */
        WAITING_FOR_ACCEPT,
        /**
         * The <code>CONNECTING</code> is set for both parties after remote 
         * user accepts the file transfer.
         */
        CONNECTING,
        /**
         * The <code>TRANSFERRING</code> is set at the start of the file transfer.
         */
        TRANSFERRING,
        /**
         * The <code>TRANSFERRING_OVER_RELAY</code> set when no direct connection 
         * between sender and receiver could be established over the network. 
         * Analogous to TRANSFERRING.
         */
        TRANSFERRING_OVER_RELAY,
        /**
         * The <code>PAUSED</code> this status is currently unused.
         */
        PAUSED,
        /**
         * The <code>REMOTELY_PAUSED</code> this status is also currently unused.
         */
        REMOTELY_PAUSED,
        /**
         * The <code>CANCELLED</code> file transfer has been locally cancelled. 
         * Remote user status is set to FAILED and FAILURE_REASON to REMOTELY_CANCELLED.
         */
        CANCELLED,
        /**
         * The <code>COMPLETED</code> file transfer was completed.
         */
        COMPLETED,
        /**
         * The <code>FAILED</code> file transfer failed to complete. Cause of 
         * the failure can be seen in FAILUREREASON.
         */
        FAILED,
        /**
         * The <code>UNKNOWN</code> constant indicates the skype filetransfer status is unknown.
         */
        UNKNOWN;
    }
    
    /**
     * The <code>FailureReason</code> enum contains status when STATUS is set to FAILED.
     */
    public enum FailureReason {
        /**
         * The <code>SENDER_NOT_AUTHORIZED</code> It is only possible to 
         * transfer files between users who have authorized each-other. 
         * As initiating file transfers to remote users who have not authorized 
         * the sender is currently blocked by UI, this FAILUREREASON appears to be unused.
         */
        SENDER_NOT_AUTHORIZED,
        /**
         * The <code>REMOTELY_CANCELLED</code> set when remote user has cancelled the transfer.
         */
        REMOTELY_CANCELLED,
        /**
         * The <code>FAILED_READ</code> read error on local machine.
         */
        FAILED_READ,
        /**
         * The <code>FAILED_REMOTE_READ</code> read error on remote machine.
         */
        FAILED_REMOTE_READ,
        /**
         * The <code>FAILED_WRITE</code> write error on local machine.
         */
        FAILED_WRITE,
        /**
         * The <code>FAILED_REMOTE_WRITE</code> write error on remote machine.
         */
        FAILED_REMOTE_WRITE,
        /**
         * The <code>REMOTE_DOES_NOT_SUPPORT_FT</code> Skype client of the receiver does not support file transfers.
         */
        REMOTE_DOES_NOT_SUPPORT_FT,
        /**
         * The <code>REMOTE_OFFLINE_FOR_TOO_LONG</code>  the recipient of the 
         * proposed file transfer is not available (offline for longer than 7 days).
         */
        REMOTE_OFFLINE_FOR_TOO_LONG,
        /**
         * The <code>UNKNOWN</code> constant indicates the skype filetransfer failure reason is unknown.
         */
        UNKNOWN;
    }
    
    /**
     * ID of this FileTransfer object.
     */
    private final String id;

    /**
     * Constructor, please use getFileTransfer() instead.
     * @param newId ID of this FileTransfer.
     */
    private FileTransfer(String newId) {
        assert newId != null;
        this.id = newId;
    }
    
    /**
     * Overridden to provide ID as hashcode.
     * @return ID.
     */
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * Implement a equals check for FileTransfer objects based on their ID's.
     * @param compared The object to compare to.
     * @return true when objects are equal.
     */
    public boolean equals(Object compared) {
        if (compared instanceof FileTransfer) {
            return getId().equals(((FileTransfer) compared).getId());
        }
        return false;
    }

    /**
     * Return ID of this FileTransfer.
     * @return ID.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the type of the filetransfer.
     * @return the type of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }
    
    /**
     * Gets the current status of the filetransfer.
     * @return the current status of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(getProperty("STATUS"));
    }
    
    /**
     * Gets the failure reason of the filetransfer.
     * @return the failure reason of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public FailureReason getFailureReason() throws SkypeException {
        return FailureReason.valueOf(getProperty("FAILUREREASON"));
    }
    
    /**
     * Gets the remote user’s skypename.
     * @return the partner handle of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getPartnerHandle() throws SkypeException {
        return getProperty("PARTNER_HANDLE");
    }
    
    /**
     * Gets the remote user’s display name.
     * @return the partner display name of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getPartnerDisplayName() throws SkypeException {
        return getProperty("PARTNER_DISPNAME");
    }
    
    /**
     * Gets the Unix timestamp of when the transfer was started.
     * @return the start time of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getStartTime() throws SkypeException {
        return getProperty("STARTTIME");
    }
    
    /**
     * Gets the Unix timestamp of when the transfer was finished.
     * while transmission is in progress the value is updated with estimated 
     * time of completion (0 when no estimation can be given). 
     * When transmission is finished, the value is set to the timestamp of completion/failure.
     * @return the finish time of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getFinishTime() throws SkypeException {
        return getProperty("FINISHTIME");
    }
    
    /**
     * Gets the full path of the file being read or written in local file system. Includes filename and extension.
     * @return the file path of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getFilePath() throws SkypeException {
        return getProperty("FILEPATH");
    }
    
    /**
     * Gets the filename (and extension) without path. This is also seen by the 
     * receiver before accept (default file name, from sender).
     * @return the file name of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getFileName() throws SkypeException {
        return getProperty("FILENAME");
    }
    
    /**
     * Gets the file size, 64-bit numeric.
     * @return the file size of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getFileSize() throws SkypeException {
        return getProperty("FILESIZE");
    }
    
    /**
     * Gets the transfer speed during file transfer. Becomes 0 after transfer is completed, failed or aborted.
     * @return the bytes per second of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getBytesPerSecond() throws SkypeException {
        return getProperty("BYTESPERSECOND");
    }
    
    /**
     * Gets the current nr. of bytes transferred (progress), 64-bit numeric.
     * @return the bytes transferred of the filetransfer.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getBytesTransferred() throws SkypeException {
        return getProperty("BYTESTRANSFERRED");
    }
    
    /**
     * Remove this Filetransfer from the list of watchable Filetransfers.
     */
    final void dispose() {
        files.remove(getId());
    }
    
    /**
     * Method used by other methods to retrieve a property value from Skype client.
     * @param name name of the property.
     * @return value of the property.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("FILETRANSFER", getId(), name);
    }
}
