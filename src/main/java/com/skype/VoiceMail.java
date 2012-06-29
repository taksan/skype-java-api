/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API, implementation and changed javadoc
 * Bart Lamot - initial javadoc
 ******************************************************************************/
package com.skype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * Class to represent the Skype VoiceMail object.
 * @see https://developer.skype.com/Docs/ApiDoc/VOICEMAIL_object
 */
public final class VoiceMail extends SkypeObject {
	/**
     * Collection of VoiceMail objects.
     */
    private static final Map<String, VoiceMail> voiceMails = new HashMap<String, VoiceMail>();

    /**
     * Mutex of voiceMailStatusChangedListener.
     */
    private static final Object voiceMailStatusChangedListenerFieldMutex = new Object();

    /**
     * Main VoiceMailStatusChangedListener handler.
     */
    private static ConnectorListener voiceMailStatusChangedListener;
    
    /**
     * Returns the VoiceMail object by the specified id.
     * @param id the id whose associated VoiceMail object is to be returned.
     * @return VoiceMail object with ID == id.
     */
    static VoiceMail getInstance(final String id) {
        synchronized(voiceMails) {
            if (!voiceMails.containsKey(id)) {
                voiceMails.put(id, new VoiceMail(id));
            }
            return voiceMails.get(id);
        }
    }

    /**
     * Enumeration of VoiceMail types.
     */
    public enum Type {
        /**
         * INCOMING - voicemail received from partner
         * OUTGOING - voicemail sent to partner
         * DEFAULT_GREETING - Skype default greeting from partner
         * CUSTOM_GREETING - partner's recorded custom greeting
         * UNKNOWN - unknown type
         */  
        INCOMING, OUTGOING, DEFAULT_GREETING, CUSTOM_GREETING, UNKNOWN;
    }
    
    /**
     * Enumeration of VoiceMail status types.
     */
    public enum Status {
        /**
         * NOTDOWNLOADED - voicemail is stored on server (has not been downloaded yet)
         * DOWNLOADING - downloading from server to local machine
         * UNPLAYED - voicemail has been downloaded but not played back yet
         * BUFFERING - buffering for playback
         * PLAYING - currently played back
         * PLAYED - voicemail has been played back
         * BLANK - intermediate status when new object is created but recording has not begun
         * RECORDING - voicemail currently being recorded
         * RECORDED - voicemail recorded but not yet uploaded to the server
         * UPLOADING - voicemail object is currently being uploaded to server
         * UPLOADED - upload to server finished but not yet deleted; object is also locally stored
         * DELETING - pending delete
         * FAILED - downloading voicemail/greeting failed
         * UNKNOWN - unknown status
         */
        NOTDOWNLOADED, DOWNLOADING, UNPLAYED, BUFFERING, PLAYING, PLAYED, BLANK, RECORDING, RECORDED, UPLOADING, UPLOADED, DELETING, FAILED, UNKNOWN;
    }
    
    /**
     * Enumeration of VoiceMail failure reason types
     */
    public enum FailureReason {
        /**
         * MISC_ERROR
         * CONNECT_ERROR
         * NO_VOICEMAIL_PRIVILEGE
         * NO_SUCH_VOICEMAIL
         * FILE_READ_ERROR
         * FILE_WRITE_ERROR
         * RECORDING_ERROR
         * PLAYBACK_ERROR
         * UNKNOWN
         */
        MISC_ERROR, CONNECT_ERROR, NO_VOICEMAIL_PRIVILEGE, NO_SUCH_VOICEMAIL, FILE_READ_ERROR, FILE_WRITE_ERROR, RECORDING_ERROR, PLAYBACK_ERROR, UNKNOWN;
    }

    /** The ID of this VoiceMail object. */
    private final String id;

    /**
     * List of listeners for status changed event.
     */
    private final List<VoiceMailStatusChangedListener> listeners = Collections.synchronizedList(new ArrayList<VoiceMailStatusChangedListener>());
    
    /**
     * Previous status.
     */
    private Status oldStatus;

    /**
     * Exception handler.
     */
    private SkypeExceptionHandler exceptionHandler;

    /**
     * Constructor.
     * @param newId the ID of new VoiceMail object
     * @see VoiceMail#getInstance(String)
     */
    private VoiceMail(String newId) {
        this.id = newId;
    }

    /**
     * Returns the hash code value for this VoiceMail object.
     * The VoiceMail ID is used as the hash code.
     * @return the hashcode
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this VoiceMail object.
     * VoiceMail IDs are used for equalness checking.
     * @param compared the object to compare to.
     * @return  <code>true</code> if this VoiceMail object is the same as the compared argument; <code>false</code> otherwise.
     */
    public boolean equals(final Object compared) {
        if (compared instanceof VoiceMail) {
            return id.equals(((VoiceMail)compared).id);
        }
        return false;
    }

    /**
     * Returns the ID of this VoiceMail object.
     * @return the ID of this VoiceMail object
     */
    public String getId() {
        return id;
    }
    
    /**
     * Adds a listener for the status changed event.
     * The listener will be triggered every time the status of this VoiceMail object is changed.
     * @param listener the listener to be added
     * @throws SkypeException 
     */
    public void addVoiceMailStatusChangedListener(final VoiceMailStatusChangedListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized(voiceMailStatusChangedListenerFieldMutex) {
            listeners.add(listener);
            if (voiceMailStatusChangedListener == null) {
                voiceMailStatusChangedListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("VOICEMAIL ")) {
                            String data = message.substring("VOICEMAIL ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if("STATUS".equals(propertyName)) {
                                VoiceMail voiceMail = VoiceMail.getInstance(id);
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                VoiceMail.Status status = VoiceMail.Status.valueOf(propertyValue);
                                voiceMail.fireStatusChanged(status);
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(voiceMailStatusChangedListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Removes a listener for the status changed event.
     * If the listener is already removed, nothing happens.
     * @param listener the listener to be removed
     */
    public void removeVoiceMailStatusChangedListener(final VoiceMailStatusChangedListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized(voiceMailStatusChangedListenerFieldMutex) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                Connector.getInstance().removeConnectorListener(voiceMailStatusChangedListener);
                voiceMailStatusChangedListener = null;
            }
        }
    }

    /**
     * Notifies the status changed event of all listeners.
     * @param status the new status
     */
    private void fireStatusChanged(final Status status) {
        VoiceMailStatusChangedListener[] listeners = this.listeners.toArray(new VoiceMailStatusChangedListener[0]);
        if (status == oldStatus) {
            return;
        }
        oldStatus = status;
        for (VoiceMailStatusChangedListener listener : listeners) {
            try {
                listener.statusChanged(status);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    /**
     * Returns the type of this VoiceMail object.
     * @return call the type of this VoiceMail object
     * @throws SkypeException if the connection is bad
     */
    public Type getType() throws SkypeException {
        // call Utils#getPropertyWithCommandId(String, String, String) to prevent new event notification
        return Type.valueOf(Utils.getPropertyWithCommandId("VOICEMAIL", getId(), "TYPE"));
    }

    /**
     * Returns the Skype user who is the partner in this voice mail.
     * @return the partner Skype user
     * @throws SkypeException if the connection is bad
     */
    public User getPartner() throws SkypeException {
        return User.getInstance(getPartnerId());
    }

    /**
     * Returns the Skype ID who is the partner in this voice mail.
     * @return the partner Skype user
     * @throws SkypeException if the connection is bad
     */
    public String getPartnerId() throws SkypeException {
        return getProperty("PARTNER_HANDLE");
    }

    /**
     * Returns the display name of the Skype user who is the partner in this voice mail.
     * @return the diplay name of the partner Skype user
     * @throws SkypeException if the connection is bad
     */
    public String getPartnerDisplayName() throws SkypeException {
        return getProperty("PARTNER_DISPNAME");
    }

    /**
     * Returns the current status of this VoiceMail object.
     * @return the current status of this VoiceMail object
     * @throws SkypeException if connection is bad
     */
    public Status getStatus() throws SkypeException {
        // call Utils#getPropertyWithCommandId(String, String, String) to prevent new event notification
        return Status.valueOf(Utils.getPropertyWithCommandId("VOICEMAIL", getId(), "STATUS"));
    }

    /**
     * Returns the failure reason of this VoiceMail object.
     * @return the failure reason of this VoiceMail object
     * @throws SkypeException if connection is bad
     */
    public FailureReason getFailureReason() throws SkypeException {
        return FailureReason.valueOf(getProperty("FAILUREREASON"));
    }

    /**
     * Returns the start time of this VoiceMail object.
     * @return the start time of this VoiceMail object
     * @throws SkypeException if connection is bad
     */
    public Date getStartTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Returns the duration of this VoiceMail object in seconds.
     * @return the duration of this VoiceMail object
     * @throws SkypeException if connection is bad
     */
    public int getDuration() throws SkypeException {
        return Integer.parseInt(getProperty("DURATION"));
    }

    /**
     * Returns the maximum duration of this VoiceMail object in seconds allowed to leave to partner.
     * @return the maximum duration of this VoiceMail object
     * @throws SkypeException if connection is bad
     */
    public int getAllowedDuration() throws SkypeException {
        return Integer.parseInt(getProperty("ALLOWED_DURATION"));
    }

    /**
     * Returns the property of this VoiceMail object spcified by the name.
     * @param name the property name
     * @return The value of the property
     * @throws SkypeException if the connection is bad
     */
    private String getProperty(final String name) throws SkypeException {
        return Utils.getProperty("VOICEMAIL", getId(), name);
    }
    
    /**
     * Starts the playback of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void startPlayback() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "STARTPLAYBACK");
    }
    
    /**
     * Stops the playback of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void stopPlayback() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "STOPPLAYBACK");
    }
    
    /**
     * Uploads the playback of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void upload() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "UPLOAD");
    }
    
    /**
     * Downloads the playback of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void download() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "DOWNLOAD");
    }
    
    /**
     * Starts the recording of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void startRecording() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "STARTRECORDING");
    }
    
    /**
     * Stops the recording of this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void stopRecording() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "STOPRECORDING");
    }
    
    /**
     * Deletes this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void dispose() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER VOICEMAIL " + getId() + " " + "DELETE");
    }
    
    /**
     * Opens the Skype window and starts playing this VoiceMail object.
     * @throws SkypeException if the connection is bad
     */
    public void openAndStartPlayback() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN VOICEMAIL " + getId());
    }

    /**
     * Waits for finishing.
     * @throws IllegalStateException if the type is not outgoing
     * @throws SkypeException if the connection is bad
     */
    public void waitForFinishing() throws SkypeException {
        if (getType() != Type.OUTGOING) {
            throw new IllegalStateException("The type must be outgoing.");
        }
        final Object wait = new Object();
        VoiceMailStatusChangedListener listener = new VoiceMailStatusChangedListener() {
            public void statusChanged(Status status) throws SkypeException {
                synchronized(wait) {
                    switch (status) {
                        case UPLOADED:
                        case UNKNOWN:
                        case FAILED:
                            wait.notify();
                            break;
                        default:
                        	// do nothing
                    }
                }
            }
        };
        synchronized(wait) {
            switch (getStatus()) {
                case UPLOADED:
                case UNKNOWN:
                case FAILED:
                    break;
                default:
                    addVoiceMailStatusChangedListener(listener);
                    try {
                        wait.wait();
                    } catch(InterruptedException e) {
                        // do nothing
                    }
                    removeVoiceMailStatusChangedListener(listener);
                    break;
            }
        }
    }
}
