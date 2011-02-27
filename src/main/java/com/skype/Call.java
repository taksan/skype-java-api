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
 * Koji Hisano - initial API and implementation
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * This class implements all features of the SKYPE CALL protocol.
 * @see <a href="https://developer.skype.com/Docs/ApiDoc/Making_and_managing_voice_calls">Skype API reference - Commands - Making and managing voice calls</a>
 * @see <a href="https://developer.skype.com/Docs/ApiDoc/Making_and_managing_video_calls">Skype API reference - Commands - Making and managing video calls</a>
 * @see <a href="https://developer.skype.com/Docs/ApiDoc/CALL_object">Skype API reference - Objects - CALL object</a>
 * @see <a href="https://developer.skype.com/Docs/ApiDoc/Call_notifications">Skype API reference - Notifications - Object notifications - Call notifications</a>
 * @author Koji Hisano
 */
public final class Call extends SkypeObject {
    /**
     * Collection of Call objects, filled at runtime when new CALL objects are created (by events or by application).
     */
    private static final Map<String, Call> calls = new HashMap<String, Call>();
    
    /**
     * Returns the Call object by the specified id.
     * @param id whose associated Call object is to be returned.
     * @return Call object with ID == id.
     */
    static Call getInstance(final String id) {
        synchronized(calls) {
            if (!calls.containsKey(id)) {
                calls.put(id, new Call(id));
            }
            return calls.get(id);
        }
    }

	/**
	 * Enumeration of call status types.
	 */
    public enum Status {
    	/**
    	 * UNPLACED - call was never placed.
    	 * ROUTING - call is currently being routed.
    	 * EARLYMEDIA - with pstn it is possible that before a call is established, early media is played. For example it can be a calling tone or a waiting message such as all operators are busy.
    	 * FAILED - call failed - try to get a FAILUREREASON for more information.
    	 * RINGING - currently ringing.
    	 * INPROGRESS - call is in progress.
    	 * ONHOLD - call is placed on hold.
    	 * FINISHED - call is finished.
    	 * MISSED - call was missed.
    	 * REFUSED - call was refused.
    	 * BUSY - destination was busy.
    	 * CANCELLED (Protocol 2).
    	 * VM_BUFFERING_GREETING - voicemail greeting is being downloaded.
    	 * VM_PLAYING_GREETING - voicemail greeting is being played.
    	 * VM_RECORDING - voicemail is being recorded.
    	 * VM_UPLOADING - voicemail recording is finished and uploaded into server.
    	 * VM_SENT - voicemail has successfully been sent.
    	 * VM_CANCELLED - leaving voicemail has been cancelled.
    	 * VM_FAILED - leaving voicemail failed; check FAILUREREASON.
    	 * LOCALHOLD - call was placed on hold by local user.
    	 * REMOTEHOLD - call was placed on hold by remote user.
    	 * WAITING_REDIAL_COMMAND - call to PSTN was rejected by remote party.
    	 * REDIAL_PENDING - redial button on the Call Phones tab of the Skype interface is pressed.
    	 */
        UNPLACED, ROUTING, EARLYMEDIA, FAILED, RINGING, INPROGRESS, ONHOLD, FINISHED, MISSED, REFUSED, BUSY, CANCELLED, VM_BUFFERING_GREETING, VM_PLAYING_GREETING, VM_RECORDING, VM_UPLOADING, VM_SENT, VM_CANCELLED, VM_FAILED, TRANSFERRING, TRANSFERRED, LOCALHOLD, REMOTEHOLD, WAITING_REDIAL_COMMAND, REDIAL_PENDING
    }

    /**
     * Enumeration of CALL types.
     */
    public enum Type {
    	/**
    	 * INCOMING_PSTN - incoming call from PSTN.
    	 * OUTGOING_PSTN - outgoing call to PSTN.
    	 * INCOMING_P2P - incoming call from P2P.
    	 * OUTGOING_P2P - outgoing call to P2P.
    	 */	 
        INCOMING_PSTN, OUTGOING_PSTN, INCOMING_P2P, OUTGOING_P2P;
    }

    /**
     * Enumeration of video status types.
     */
    public enum VideoStatus {
    	/**
         * NOT_AVAILABLE - The user does not have video capability because video is disabled or a webcam is unplugged).
         * AVAILABLE - The user is video-capable but the video is not running (can occur during a manual send).
         * STARTING - The video is sending but is not yet running at full speed.
         * REJECTED - The receiver rejects the video feed (can occur during a manual receive).
         * RUNNING - The video is actively running.
         * STOPPING - The active video is in the process of stopping but has not halted yet.
         * PAUSED - The video call is placed on hold.
    	 */
        NOT_AVAILABLE, AVAILABLE, STARTING, REJECTED, RUNNING, STOPPING, PAUSED
    }
    
    /**
     * Enumeration of DTMF types.
     */
    public enum DTMF {
        TYPE_0, TYPE_1, TYPE_2, TYPE_3, TYPE_4, TYPE_5, TYPE_6, TYPE_7, TYPE_8, TYPE_9, TYPE_SHARP('#'), TYPE_ASTERISK('*');
        
        private final char type;
        
        DTMF() {
            type = name().charAt("TYPE_".length());
        }
        
        DTMF(char type) {
            this.type = type;
        }
        
        char getType() {
            return type;
        }
    }

    /**
     * Enumeration of video enabled status types.
     */
    private enum VideoEnabledStatus {
    	/**
    	 * VIDEO_NONE.
    	 * VIDEO_SEND_ENABLED.
    	 * VIDEO_RECV_ENABLED.
    	 * VIDEO_BOTH_ENABLED.
    	 */
        VIDEO_NONE, VIDEO_SEND_ENABLED, VIDEO_RECV_ENABLED, VIDEO_BOTH_ENABLED;
    }
    
    /**
     * The CALL objects ID.
     */
    private final String id;

    /**
     * List of listeners to CALL objects.
     */
    private final List<CallStatusChangedListener> listeners = Collections.synchronizedList(new ArrayList<CallStatusChangedListener>());
    
    /**
     * Previous status.
     */
    private Status oldStatus;
    
    /**
     * Exception handler to CALL object.
     */
    private SkypeExceptionHandler exceptionHandler;
    
    /**
     * Flag for fired events.
     */
    private boolean isCallListenerEventFired;

    /**
     * Consturctor.
     * Use getInstance instead of constructor.
     * @param newId the ID of this CALL object.
     */
    private Call(final String newId) {
        this.id = newId;
    }

    /**
     * Use the CALL ID as the hashcode.
     * @return id.
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Implement a equals check method.
     * Check ID field for equalness.
     * @param compared the object to compare to.
     * @return true if objects are equal.
     */
    public boolean equals(final Object compared) {
        if (compared instanceof Call) {
            return id.equals(((Call) compared).id);
        }
        return false;
    }

    /**
     * Return the ID of the CALL object.
     * @return the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Add a listener for the Status field.
     * The listener will be triggered every time the status of this CALL object is changed.
     * @param listener the listener to add.
     */
    public void addCallStatusChangedListener(final CallStatusChangedListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.add(listener);
    }

    /**
     * Remove a listener to the status of this CALL object.
     * If listener is already removed nothing happens.
     * @param listener the listener to remove.
     */
    public void removeCallStatusChangedListener(final CallStatusChangedListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    /**
     * Trigger all Status listeners because the status of this CALL object has changed.
     * @param status the new status.
     */
    void fireStatusChanged(final Status status) {
        CallStatusChangedListener[] listeners = this.listeners.toArray(new CallStatusChangedListener[0]);
        if (status == oldStatus) {
            return;
        }
        oldStatus = status;
        for (CallStatusChangedListener listener : listeners) {
            try {
                listener.statusChanged(status);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    /**
     * Put this CALL on hold.
     * @throws SkypeException when connection is bad.
     */
    public void hold() throws SkypeException {
        setStatus("ONHOLD");
    }

    /**
     * Resume an on hold CALL.
     * @throws SkypeException when connection is bad.
     */
    public void resume() throws SkypeException {
        setStatus("INPROGRESS");
    }

    /**
     * End a CALL.
     * @throws SkypeException when connection is bad.
     */
    public void finish() throws SkypeException {
        setStatus("FINISHED");
    }

    /**
     * Answer a ringing CALL.
     * @throws SkypeException when connection is bad.
     */
    public void answer() throws SkypeException {
        setStatus("INPROGRESS");
    }

    /**
     * Cancel a CALL.
     * @throws SkypeException when connection is bad.
     */
    public void cancel() throws SkypeException {
        setStatus("FINISHED");
    }

    /**
     * Change the status of this CALL object.
     * @param status The new status to set.
     * @throws SkypeException when connection is bad.
     */
    private void setStatus(final String status) throws SkypeException {
        try {
            String response = Connector.getInstance().executeWithId("SET CALL " + getId() + " STATUS " + status, "CALL " + getId() + " STATUS ");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Forward a ringing CALL to profile forwarding rule.
     * @throws SkypeException when connection is bad.
     */
    public void forward() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER CALL " + getId() + " END FORWARD_CALL");
    }
    
    /**
     * Redirect a ringing CALL to a voice mail.
     * @throws SkypeException when connection is bad.
     */
    public void redirectToVoiceMail() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER CALL " + getId() + " END FORWARD_CALL");
    }
    
    /**
     * Send a DTMF command.
     * @throws SkypeException when connection is bad.
     */
    public void send(DTMF command) throws SkypeException {
        Utils.executeWithErrorCheck("SET CALL " + getId() + " DTMF " + command.getType());
    }

    /**
     * Get the starttime of this CALL object.
     * @return the starttime.
     * @throws SkypeException when connection is bad.
     */
    public Date getStartTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Return the Skype user who is the partner in this CALL.
     * @return the other Skype user.
     * @throws SkypeException when connection is bad.
     */
    public User getPartner() throws SkypeException {
        return User.getInstance(getPartnerId());
    }

    /**
     * Return the Skype handle of the other user in this CALL.
     * @return The handle.
     * @throws SkypeException when connection is bad.
     */
    public String getPartnerId() throws SkypeException {
        return getProperty("PARTNER_HANDLE");
    }

    /**
     * Return the DISPLAYNAME of the other user in this CALL.
     * @return DISPLAYNAME.
     * @throws SkypeException when connection is bad.
     */
    public String getPartnerDisplayName() throws SkypeException {
        return getProperty("PARTNER_DISPNAME");
    }

    /**
     * Return the type of this call.
     * @return call type.
     * @throws SkypeException when connection is bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Return the current status of this CALL.
     * @return Status of this call.
     * @throws SkypeException when connection is bad.
     */
    public Status getStatus() throws SkypeException {
        // call Utils#getPropertyWithCommandId(String, String, String) to prevent new event notification
        return Status.valueOf(Utils.getPropertyWithCommandId("CALL", getId(), "STATUS"));
    }

    /**
     * Return the duration of this CALL.
     * @return duration of this call.
     * @throws SkypeException when connection is bad.
     */
    public int getDuration() throws SkypeException {
        return Integer.parseInt(getProperty("DURATION"));
    }

    /**
     * Return the reason of failure.
     * @return FAILUREREASON.
     * @throws SkypeException when connection is bad.
     */
    public int getErrorCode() throws SkypeException {
        return Integer.parseInt(getProperty("FAILUREREASON"));
    }

    /**
     * Start or stop receiving video on this call.
     * @param videoStatus enable = true.
     * @throws SkypeException when connection is bad.
     */
    public void setReceiveVideoEnabled(final boolean videoStatus) throws SkypeException {
        String value = videoStatus ? "START_VIDEO_SEND" : "STOP_VIDEO_SEND";
        try {
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " " + value);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Check if video receiving is enabled for this CALL.
     * @return true if enabled.
     * @throws SkypeException when connection is bad.
     */
    public boolean isReceiveVideoEnabled() throws SkypeException {
        VideoEnabledStatus enabled = VideoEnabledStatus.valueOf(getProperty("VIDEO_STATUS"));
        switch (enabled) {
        case VIDEO_NONE:
        case VIDEO_SEND_ENABLED:
            return false;
        case VIDEO_RECV_ENABLED:
        case VIDEO_BOTH_ENABLED:
            return true;
        default:
            return false;
        }
    }

    /**
     * Start or stop sending video with this call.
     * @param videoStatus enable = true.
     * @throws SkypeException when connection is bad.
     */
    public void setSendVideoEnabled(final boolean videoStatus) throws SkypeException {
        String value = videoStatus ? "START_VIDEO_RECEIVE" : "STOP_VIDEO_RECEIVE";
        try {
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " " + value);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Check if video sending is enabled for this CALL.
     * @return true if enabled.
     * @throws SkypeException when connection is bad.
     */
    public boolean isSendVideoEnabled() throws SkypeException {
        VideoEnabledStatus enabled = VideoEnabledStatus.valueOf(getProperty("VIDEO_STATUS"));
        switch (enabled) {
        case VIDEO_NONE:
        case VIDEO_RECV_ENABLED:
            return false;
        case VIDEO_SEND_ENABLED:
        case VIDEO_BOTH_ENABLED:
            return true;
        default:
            return false;
        }
    }

    /**
     * Return the status of receiving video with this CALL.
     * @return videoStatus of this call.
     * @throws SkypeException when connection is bad.
     */
    public VideoStatus getReceiveVideoStatus() throws SkypeException {
        return VideoStatus.valueOf(getProperty("VIDEO_RECEIVE_STATUS"));
    }

    /**
     * Return the status of sending video with this CALL.
     * @return videoStatus of this call.
     * @throws SkypeException when connection is bad.
     */
    public VideoStatus getSendVideoStatus() throws SkypeException {
        return VideoStatus.valueOf(getProperty("VIDEO_SEND_STATUS"));
    }
    
    /**
     * Return the conference ID of this CALL.
     * @return The conference ID
     * @throws SkypeException when connection is bad.
     */
    public String getConferenceId() throws SkypeException {
        return getProperty("CONF_ID");
    }
    
    public String getParticipantsCount() throws SkypeException {
        return getProperty("CONF_PARTICIPANTS_COUNT");
    }

    /**
     * Return property of this CALL.
     * @param name property name.
     * @return The value of the property.
     * @throws SkypeException when connection is bad.
     */
    private String getProperty(final String name) throws SkypeException {
        return Utils.getProperty("CALL", getId(), name);
    }
    
    /**
     * Check if an event is fired.
     * @return true if fired.
     */
    boolean isCallListenerEventFired() {
        return isCallListenerEventFired;
    }

    /**
     * Set the eventfired flag.
     * @param fired the value of the flag.
     */
    void setCallListenerEventFired(final boolean fired) {
        isCallListenerEventFired = fired;
    }
    
    public void setFileInput(File file) throws SkypeException {
        if (file == null) {
            clearFileInput();
            return;
        }
        Map<String, String> inputStreams = getInputStreams();
        inputStreams.put("FILE", file.getAbsolutePath());
        setInputStreams(inputStreams);
    }
    
    public void clearFileInput() throws SkypeException {
        Map<String, String> inputStreams = getInputStreams();
        inputStreams.remove("FILE");
        setInputStreams(inputStreams);
    }

    public void setFileOutput(File file) throws SkypeException {
        if (file == null) {
            clearFileOutput();
            return;
        }
        Map<String, String> outputStreams = getOutputStreams();
        outputStreams.put("FILE", file.getAbsolutePath());
        setOutputStreams(outputStreams);
    }
    
    public void clearFileOutput() throws SkypeException {
        Map<String, String> outputStreams = getOutputStreams();
        outputStreams.remove("FILE");
        setOutputStreams(outputStreams);
    }
    
    public void setFileCaptureMic(File file) throws SkypeException {
        if (file == null) {
            clearFileCaptureMic();
            return;
        }
        Map<String, String> captureMicStreams = getCaptureMicStreams();
        captureMicStreams.put("FILE", file.getAbsolutePath());
        setCaptureMicStreams(captureMicStreams);
    }
    
    public void clearFileCaptureMic() throws SkypeException {
        Map<String, String> inputStreams = getCaptureMicStreams();
        inputStreams.remove("FILE");
        setCaptureMicStreams(inputStreams);
    }

    public void setPortInput(int port) throws SkypeException {
        if (port <= 0) {
            clearPortInput();
            return;
        }
        Map<String, String> inputStreams = getInputStreams();
        inputStreams.put("PORT", "" + port);
        setInputStreams(inputStreams);
    }
    
    public void clearPortInput() throws SkypeException {
        Map<String, String> inputStreams = getInputStreams();
        inputStreams.remove("PORT");
        setInputStreams(inputStreams);
    }

    public void setPortOutput(int port) throws SkypeException {
        if (port <= 0) {
            clearPortOutput();
            return;
        }
        Map<String, String> outputStreams = getOutputStreams();
        outputStreams.put("PORT", "" + port);
        setOutputStreams(outputStreams);
    }
    
    public void clearPortOutput() throws SkypeException {
        Map<String, String> outputStreams = getOutputStreams();
        outputStreams.remove("PORT");
        setOutputStreams(outputStreams);
    }

    public void setPortCaptureMic(int port) throws SkypeException {
        if (port <= 0) {
            clearPortCaptureMic();
            return;
        }
        Map<String, String> captureMicStreams = getCaptureMicStreams();
        captureMicStreams.put("PORT", "" + port);
        setCaptureMicStreams(captureMicStreams);
    }
    
    public void clearPortCaptureMic() throws SkypeException {
        Map<String, String> captureMicStreams = getCaptureMicStreams();
        captureMicStreams.remove("PORT");
        setCaptureMicStreams(captureMicStreams);
    }
    
    private Map<String, String> getInputStreams() throws SkypeException {
        return getStreams("INPUT");
    }
    
    private Map<String, String> getOutputStreams() throws SkypeException {
        return getStreams("OUTPUT");
    }
    
    private Map<String, String> getCaptureMicStreams() throws SkypeException {
        return getStreams("CAPTURE_MIC");
    }

    private Map<String, String> getStreams(String type) throws SkypeException {
        String response = Utils.getProperty("CALL", getId(), type);
        Utils.checkError(response);
        if("".equals(response)) {
            return new HashMap<String, String>();
        }
        Map<String, String> r = new HashMap<String, String>();
        for(String element: response.split(", ")) {
            int index = element.indexOf('=');
            String key = element.substring(0, index);
            String value = element.substring(index + 2, element.length() - 1); // remove first and last double quotation mark
            r.put(key, value);
        }
        return r;
    }

    private void setInputStreams(Map<String, String> inputStreams) throws SkypeException {
        setStreams("INPUT", inputStreams);
    }

    private void setOutputStreams(Map<String, String> outputStreams) throws SkypeException {
        setStreams("OUTPUT", outputStreams);
    }

    private void setCaptureMicStreams(Map<String, String> captureMicStreams) throws SkypeException {
        setStreams("CAPTURE_MIC", captureMicStreams);
    }

    private void setStreams(String type, Map<String, String> streams) throws SkypeException {
        try {
            StringBuilder parameters = new StringBuilder();
            for(String key: streams.keySet()) {
                parameters.append(" ");
                parameters.append(key);
                parameters.append("=\"");
                parameters.append(streams.get(key));
                parameters.append("\"");
            }
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " SET_" + type + parameters);
            Utils.checkError(response);
        } catch(ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
    
    public boolean canTransferTo(String skypeId) throws SkypeException {
        try {
            String command = "GET CALL " + getId() + " CAN_TRANSFER " + skypeId;
            String responseHeader = "CALL " + getId() + " CAN_TRANSFER " + skypeId + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            return Boolean.parseBoolean(response.substring((responseHeader).length()));
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    public void transferTo(String... skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        try {
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " TRANSFER \"" + Utils.convertToCommaSeparatedString(skypeIds) + "\"");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
