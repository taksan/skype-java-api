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
 ******************************************************************************/
package com.skype;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Implementation of the Skype SMS object (Skype API 2.5).
 * Use this class to send and receive SMS message through Skype.
 * @see https://developer.skype.com/Docs/DevNotes?action=AttachFile&do=get&target=DevNotes-2.5beta.pdf
 */
public final class SMS extends SkypeObject {
    /**
     * Collection of SMS objects.
     */
    private static final Map<String, SMS> smses = new HashMap<String, SMS>();
    
    /**
     * Returns the SMS object by the specified id.
     * @param id whose associated SMS object is to be returned.
     * @return SMS object with ID == id.
     */
    static SMS getInstance(final String id) {
        synchronized(smses) {
            if (!smses.containsKey(id)) {
                smses.put(id, new SMS(id));
            }
            return smses.get(id);
        }
    }

    /**
     * Enumeration of SMS types.
     */
    public enum Type {
    	/**
    	 * INCOMING	- An incomming sms message.
    	 * OUTGOING - A sent SMS message.
    	 * CONFIRMATION_CODE_REQUEST - A special SMS to link cell phone number with skype.
    	 * CONFIRMATION_CODE_SUBMIT - A special Message to confirm link cell phone number with skype.
    	 * UNKNOWN - Type of this SMS is unknown.
    	 */
        INCOMING, OUTGOING, CONFIRMATION_CODE_REQUEST, CONFIRMATION_CODE_SUBMIT, UNKNOWN;
    }

    /**
     * Enumeration of SMS Status.
     */
    public enum Status {
    	/**
    	 * RECEIVED	- SMS is received.
    	 * READ - SMS is read.
    	 * COMPOSING - SMS is being written (in Skype client).
    	 * SENDING_TO_SERVER - Sending the SMS to the SMS server.
    	 * SENT_TO_SERVER - SMS has been sent to the SMS server.
    	 * DELIVERED - SMS has been delivered to other party.
    	 * SOME_TARGETS_FAILED - SMS has not been deliverd to all parties.
    	 * FAILED - SMS could not be sent.
    	 * UNKNOWN - Status unknown.
    	 */
        RECEIVED, READ, COMPOSING, SENDING_TO_SERVER, SENT_TO_SERVER, DELIVERED, SOME_TARGETS_FAILED, FAILED, UNKNOWN;
    }

    /**
     * Enumeration of SMS failure reason.
     */
    public enum FailureReason {
    	/**
    	 * MISC_ERROR - Error not specified.
    	 * SERVER_CONNECT_FAILED - Skype could not connect to SMS server.
    	 * NO_SMS_CAPABILITY - SMS is not supported.
    	 * INSUFFICIENT_FUNDS - User has not enough Skype credits.
    	 * INVALID_CONFIRMATION_CODE - Confirmation code to link cell phone number to Skype ID is incorrect.
    	 * USER_BLOCKED - User is blocked.
    	 * IP_BLOCKED - IP of user is blocked.
    	 * NODE_BLOCKED - Node of user is blocked.
    	 * UNKNOWN - Something went wrong but who knows.
    	 */
        MISC_ERROR, SERVER_CONNECT_FAILED, NO_SMS_CAPABILITY, INSUFFICIENT_FUNDS, INVALID_CONFIRMATION_CODE, USER_BLOCKED, IP_BLOCKED, NODE_BLOCKED, UNKNOWN;
    }

    /**
     * Inner class to determine the target status.
     */
    public static final class TargetStatus {
    	/**
    	 * Enumeration of SMS target status. 
    	 */
        public enum Status {
        	/**
        	 * TARGET_ANALYZING - Analyzing target; country, server, sms service capable.
        	 * TARGET_UNDEFINED - Target not yet set.
        	 * TARGET_ACCEPTABLE - Target is ok to send.
        	 * TARGET_NOT_ROUTABLE - Cannot send to target, no SMS server found.
        	 * TARGET_DELIVERY_PENDING - SMS is being sent.
        	 * TARGET_DELIVERY_SUCCESSFUL - SMS has been received.
        	 * TARGET_DELIVERY_FAILED - SMS could not be delivered.
        	 * UNKNOWN - Unknown status.
        	 */
            TARGET_ANALYZING, TARGET_UNDEFINED, TARGET_ACCEPTABLE, TARGET_NOT_ROUTABLE, TARGET_DELIVERY_PENDING, TARGET_DELIVERY_SUCCESSFUL, TARGET_DELIVERY_FAILED, UNKNOWN;
        }
        
        /** ID of this Target status. */
        private final String number;
        /** Status of this target. */
        private final Status status;
        
        /**
         * Constructor.
         * @param newNumber ID of this TargetStatus.
         * @param newStatus Status to set initialy.
         */
        TargetStatus(String newNumber, Status newStatus) {
            assert newNumber != null;
            assert newStatus != null;
            this.number = newNumber;
            this.status = newStatus;
        }

        /**
         * Overridden to provide ID number as hashcode.
         * @return number + status as hashcode.
         */
        public int hashCode() {
            return (number + "/" + status).hashCode();
        }

        /**
         * Compare based on the ID numbers and not Objects or references.
         * @param compared The object to compare this SMS to.
         * @return true if number are equal.
         */
        public boolean equals(Object compared) {
            if (compared instanceof TargetStatus) {
                TargetStatus comparedTargetStatus = (TargetStatus)compared;
                return comparedTargetStatus.number.equals(number) && comparedTargetStatus.status.equals(status);
            }
            return false;
        }

        /**
         * Return the ID of this target status.
         * @return number of this target status.
         */
        public String getNumber() {
            return number;
        }

        /**
         * Return the status of this target status.
         * @return status.
         */
        public Status getStatus() {
            return status;
        }
    }

    /** Unique ID to identify this SMS. */
    private final String id;

    /**
     * Constructor.
     * @param newId unique identification number of this SMS.
     */
    private SMS(String newId) {
        assert newId != null;
        this.id = newId;
    }

    /**
     * Overridden to use ID as the hashcode.
     * @return ID.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Overridden to compare ID of SMS objects.
     * @param compared the object to compare this SMS to.
     * @return true if objects have the same ID.
     */
    public boolean equals(Object compared) {
        if (compared instanceof SMS) {
            return getId().equals(((SMS) compared).getId());
        }
        return false;
    }

    /**
     * Return the unique ID of this SMS object.
     * @return ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the body of this SMS message.
     * @return BODY of this SMS message.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public String getContent() throws SkypeException {
        return getProperty("BODY");
    }

    /**
     * Set the BODY of this SMS message.
     * @param newValue The new content.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    void setContent(String newValue) throws SkypeException {
        setSMSProperty("BODY", newValue);
    }

    /**
     * Return the type of this SMS message.
     * @return type of SMS.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Return the status of this SMS message.
     * @return status of SMS.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(getProperty("STATUS"));
    }

    /**
     * Return the failure reason of sending this SMS.
     * @return failure reason.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public FailureReason getFailureReason() throws SkypeException {
        return FailureReason.valueOf(getProperty("FAILUREREASON"));
    }

    /**
     * Check if SMS is seen.
     * @return true if SMS is seen.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public boolean isCheckedFailure() throws SkypeException {
        return !Boolean.parseBoolean(getProperty("IS_FAILED_UNSEEN"));
    }

    /**
     * Set SMS IS_FAILED_UNSEEN to TRUE.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public void toCheckedFailure() throws SkypeException {
        try {
            String command = "SET SMS " + getId() + " SEEN";
            String response = Connector.getInstance().execute(command);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Return the timestamp of this SMS.
     * @return timestamp of SMS.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    /**
     * Return the costs of this SMS to send.
     * @return the costs.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public BigDecimal getPrice() throws SkypeException {
        return new BigDecimal(getProperty("PRICE")).scaleByPowerOfTen(-Integer.parseInt(getProperty("PRICE_PRECISION")));
    }

    /**
     * Return the costs currency.
     * @return currency.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public String getCurrencyUnit() throws SkypeException {
        return getProperty("PRICE_CURRENCY");
    }

    /**
     * The phone number to reply to if an SMS is received.
     * @return phone number of other party.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public String getReplyToNumber() throws SkypeException {
        return getProperty("REPLY_TO_NUMBER");
    }

    /**
     * Set the phone number the other party can reply to.
     * @param newValue phone number to reply to. 
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public void setReplyToNumber(String newValue) throws SkypeException {
        setSMSProperty("REPLY_TO_NUMBER", newValue);
    }

    /**
     * Return an array of phonenumbers to send this SMS to.
     * @return array of target phone numbers.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public String[] getAllTargetNumbers() throws SkypeException {
        return getProperty("TARGET_NUMBERS").split(", ");
    }

    /**
     * Set the target phone numbers.
     * @param newValues the numbers to set as target.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public void setAllTargetNumbers(String[] newValues) throws SkypeException {
        setSMSProperty("TARGET_NUMBERS", Utils.convertToCommaSeparatedString(newValues));
    }

    /**
     * Return an array of Status for each target phone number.
     * @return array of traget status.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public TargetStatus[] getAllTargetStatuses() throws SkypeException {
        String data = getProperty("TARGET_STATUSES");
        List<TargetStatus> r = new ArrayList<TargetStatus>();
        for (String targetStatus: data.split(", ")) {
            String[] elements = targetStatus.split("=");
            r.add(new TargetStatus(elements[0], TargetStatus.Status.valueOf(elements[1])));
        }
        return r.toArray(new TargetStatus[0]);
    }

    /**
     * When an SMS message is too large it is split up in multiple SMS messages.
     * This method returns the message in one string array.
     * @return an array containing all chunks.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public String[] getAllContentChunks() throws SkypeException {
        int chunkCount = Integer.parseInt(getProperty("CHUNKING"));
        String[] r = new String[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            r[i] = getProperty("CHUNK " + i);
        }
        return r;
    }

    /**
     * Send this SMS message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    void send() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER SMS " + getId() + " SEND");
    }

    /**
     * Delete this SMS message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
    public void delete() throws SkypeException {
        Utils.executeWithErrorCheck("DELETE SMS " + getId());
    }

    /**
     * Retrieve the value of a SMS property.
     * @param name Name of the property to check.
     * @return the value of the property.
     * @throws SkypeException when the connection to the Skype client has gone bad or if property does not exist.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("SMS", getId(), name);
    }

    /**
     * Set the value of a SMS property.
     * @param name Name of the SMS property.
     * @param value Value of the property.
     * @throws SkypeException when the connection to the Skype client has gone bad or if property does not exist or value is invalid.
     */
    private void setSMSProperty(String name, String value) throws SkypeException {
        Utils.setProperty("SMS", getId(), name, value);
    }
}
