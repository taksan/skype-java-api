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

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * The <code>Profile</code> class contains the current user's information.
 * <p>
 * For example, you can get the mood message of the current user by this code:
 * <pre>String moodMessage = Skype.getProfile().getMoodMessage();</pre>
 * And you can change it by the following code:
 * <pre>Skype.getProfile().setMoodMessage("Happy!");</pre>
 * </p>
 * @author Koji Hisano
 */
public final class Profile {
    /** Identifies the status property. */
    public static final String STATUS_PROPERTY = "status";
    /** Identifies the mood message property. */
    public static final String MOOD_TEXT_PROPERTY = "moodText";

    /**
     * The <code>Status</code> enum contains the online status constants of the current user.
     * @see Profile#getStatus()
     * @see Profile#setStatus(Status)k
     */
    public enum Status {
        /**
         * The <code>UNKNOWN</code> constant indicates the user status is unknown.
         */
        UNKNOWN,
        /**
         * The <code>ONLINE</code> constant indicates the user is online.
         */
        ONLINE,
        /**
         * The <code>OFFLINE</code> constant indicates the user is offline.
         */
        OFFLINE,
        /**
         * The <code>SKYPEME</code> constant indicates the user is in SkypeMe mode.
         */
        SKYPEME,
        /**
         * The <code>AWAY</code> constant indicates the user is away.
         */
        AWAY,
        /**
         * The <code>NA</code> constant indicates the user is not available.
         */
        NA,
        /**
         * The <code>DND</code> constant indicates the user is in do not disturb mode.
         */
        DND,
        /**
         * The <code>INVISIBLE</code> constant indicates the user is invisible to others.
         */
        INVISIBLE,
        /**
         * The <code>LOGGEDOUT</code> constant indicates the user is logged out.
         */
        LOGGEDOUT;
    }

    /**
     * The <code>Sex</code> enum contains the sex constants of the current user.
     * @see Profile#getSex()
     * @see Profile#setSex(Sex)
     */
    public enum Sex {
        /**
         * The <code>UNKNOWN</code> constant indicates the sex of the current user is unknown.
         */
        UNKNOWN,
        /**
         * The <code>MALE</code> constant indicates the current user is male.
         */
        MALE,
        /**
         * The <code>FEMALE</code> constant indicates the current user is female.
         */
        FEMALE;
    }

    /**
     * The <code>CallForwardingRule</code> class contains the information of a call forwarding rule.
     */
    public static final class CallForwardingRule {
        /** startSecond value. */
    	private final int startSecond;
    	/** endSecond value. */
    	private final int endSecond;
        /** target String. */
    	private final String target;

        /**
         * Constructs a call forwarding rule.
         * @param newStartSecond the time in seconds when connecting to this number/user starts.
         * @param newEndSecond the time in seconds when ringing to this number/user ends.
         * @param newTarget the target Skype username to forward calls to, or the PSTN number to forward a call.
         */
        public CallForwardingRule(int newStartSecond, int newEndSecond, String newTarget) {
            this.startSecond = newStartSecond;
            this.endSecond = newEndSecond;
            if (newTarget.startsWith("+")) {
                newTarget = newTarget.replaceAll("-", "");
            }
            this.target = newTarget;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object compared) {
            if (compared instanceof CallForwardingRule) {
                return toString().equals(((CallForwardingRule)compared).toString());
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return startSecond + "," + endSecond + "," + target;
        }

        /**
         * Gets the time in seconds when connecting to this number/user starts.
         * @return the time in seconds when connecting to this number/user starts.
         */
        public int getStartSecond() {
            return startSecond;
        }

        /**
         * Gets the time in seconds when ringing to this number/user ends.
         * @return the time in seconds when ringing to this number/user ends.
         */
        public int getEndSecond() {
            return endSecond;
        }

        /**
         * Gets the target Skype username to forward calls to, or the PSTN number to forward a call.
         * @return the target Skype username to forward calls to, or the PSTN number to forward a call.
         */
        public String getTarget() {
            return target;
        }
    }
    
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private Object propertyChangeListenerMutex = new Object();
    private ConnectorListener propertyChangeListener;

    /**
     * Constructor.
     *
     */
    Profile() {
    }

    /**
     * Gets the online status of the current user.
     * @return the online status of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setStatus(Status)
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getProperty("USERSTATUS"));
    }

    /**
     * Sets the online status of the current user by the {@link Status} enum.
     * @param newValue the new online status of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getStatus()
     */
    public void setStatus(final Status newValue) throws SkypeException {
        Utils.checkNotNull("newValue", newValue);
        Utils.setProperty("USERSTATUS", newValue.toString());
    }

    /**
     * Gets the Skype ID (username) of the current user. 
     * @return the Skype ID (username) of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getId() throws SkypeException {
        return Utils.getProperty("CURRENTUSERHANDLE");
    }

    /**
     * Indicates whether the current user can do SkypeOut.
     * @return <code>true</code> if the current user can do SkypeOut; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public boolean canDoSkypeOut() throws SkypeException {
        return canDo("SKYPEOUT");
    }

    /**
     * Indicates whether the current user can do SkypeIn.
     * @return <code>true</code> if the current user can do SkypeIn; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public boolean canDoSkypeIn() throws SkypeException {
        return canDo("SKYPEIN");
    }

    /**
     * Indicates whether the current user can do VoiceMail.
     * @return <code>true</code> if the current user can do VoiceMail; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public boolean canDoVoiceMail() throws SkypeException {
        return canDo("VOICEMAIL");
    }

    /**
     * Check for a privilege.
     * @param name The name of the privilege to check.
     * @return true if this privilege is ok.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    private boolean canDo(final String name) throws SkypeException {
        return Boolean.parseBoolean(Utils.getProperty("PRIVILEGE", name));
    }

    /**
     * Gets the balance of the current user.
     * @return the balance of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    @Deprecated
    public int getPSTNBalance() throws SkypeException {
        return Integer.parseInt(getProperty("PSTN_BALANCE"));
    }

    /**
     * Gets the credit of the current user.
     * @return the balance of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public int getCredit() throws SkypeException {
        return getPSTNBalance();
    }

    /**
     * Gets the currency code of the current user.
     * @return the currency code of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    @Deprecated
    public String getPSTNBalanceCurrencyUnit() throws SkypeException {
        return getProperty("PSTN_BALANCE_CURRENCY");
    }

    /**
     * Gets the credit currency unit of the current user.
     * @return the currency code of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String getCreditCurrencyUnit() throws SkypeException {
        return getPSTNBalanceCurrencyUnit();
    }

    /**
     * Gets the full name of the current user.
     * @return the full name of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setFullName(String)
     */
    public String getFullName() throws SkypeException {
        return getProperty("FULLNAME");
    }

    /**
     * Sets the full name of the current user.
     * @param newValue the new full name of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getFullName()
     */
    public void setFullName(final String newValue) throws SkypeException {
        setProperty("FULLNAME", newValue);
    }

    /**
     * Gets the birth day of the current user.
     * @return the birth day of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setBirthDay(Date)
     */
    public Date getBirthDay() throws SkypeException {
        String value = getProperty("BIRTHDAY");
        if ("0".equals(value)) {
            return null;
        } else {
            try {
                return new SimpleDateFormat("yyyyMMdd").parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException("The library developer should check Skype specification.");
            }
        }
    }

    /**
     * Sets the birth day of the current user.
     * @param newValue the new birth day of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getBirthDay()
     */
    public void setBirthDay(final Date newValue) throws SkypeException {
        String newValueString;
        if (newValue == null) {
            newValueString = "0";
        } else {
            newValueString = new SimpleDateFormat("yyyyMMdd").format(newValue);
        }
        setProperty("BIRTHDAY", newValueString);
    }

    /**
     * Gets the sex of the current user.
     * @return the sex of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setSex(Sex)
     */
    public Sex getSex() throws SkypeException {
        return Sex.valueOf(getProperty("SEX"));
    }

    /**
     * Sets the sex of the current user by the {@link Sex} enum.
     * @param newValue the new sex of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getSex()
     */
    public void setSex(final Sex newValue) throws SkypeException {
        Utils.checkNotNull("newValue", newValue);
        setProperty("SEX", newValue.toString());
    }

    /**
     * Gets the all languages of the current user.
     * @return the all languages of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setAllLanguages(String[])
     */
    @Deprecated
    public String[] getAllLauguages() throws SkypeException {
        return getProperty("LANGUAGES").split(" ");
    }
    
    /**
     * Get the language by ISO code of the current user.
     * @return the language of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setLanguage(String)
     */
    public String getLanguageByISOCode() throws SkypeException  {
        return getAllLauguages()[0];
    }

    /**
     * Sets the all languages of the current user.
     * @param newValues the all new languages of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getAllLauguages()
     */
    @Deprecated
    public void setAllLanguages(String[] newValues) throws SkypeException {
        if (newValues == null) {
            newValues = new String[0];
        }
        setProperty("LANGUAGES", toSpaceSeparatedString(newValues));
    }

    /**
     * Sets the language by ISO code of the current user.
     * @param newValues the language by ISO code of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getAllLauguages()
     */
    public void setLanguageByISOCode(final String newValue) throws SkypeException {
        setProperty("LANGUAGES", newValue);
    }

    /**
     * List a object in a space seperated String.
     * @param newValues objects to list.
     * @return String with whitespaces and objectnames.
     */
    private String toSpaceSeparatedString(final Object[] newValues) {
        StringBuilder newValuesString = new StringBuilder();
        for (int i = 0; i < newValues.length; i++) {
            if (i != 0) {
                newValuesString.append(' ');
            }
            newValuesString.append(newValues[i]);
        }
        return newValuesString.toString();
    }

    /**
     * Gets the country of the current user by the ISO code.
     * @return the country of the current user by the ISO code.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setCountryByISOCode(String)
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
     */
    public String getCountryByISOCode() throws SkypeException {
        String value = getProperty("COUNTRY");
        return value.substring(0, value.indexOf(' '));
    }

    /**
     * Gets the IP country of the current user by the ISO code.
     * @return the country of the current user by the ISO code.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
     */
    public String getIPCountryByISOCode() throws SkypeException {
        return getProperty("IPCOUNTRY");
    }

    /**
     * Sets the country of the current user by the ISO code.
     * @param newValue the new country of the current user by the ISO code.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getCountryByISOCode()
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
     */
    public void setCountryByISOCode(final String newValue) throws SkypeException {
        setProperty("COUNTRY", Utils.convertNullToEmptyString(newValue));
    }

    /**
     * Gets the country of the current user.
     * @return the country of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setCountry(String)
     */
    public String getCountry() throws SkypeException {
        String value = getProperty("COUNTRY");
        return value.substring(value.indexOf(' ') + 1);
    }

    /**
     * Sets the country of the current user. The given country name must always be in english
     * (to avoid locale problems).
     * 
     * @param newValue the new country of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getCountry()
     */
    public void setCountry(final String countryName) throws SkypeException {
    	Utils.checkNotNull("countryName", countryName);
    	String isoForCountry = CountryIsoByCountryName.getIsoForCountry(countryName);
		Utils.checkNotNull("isoForCountry", isoForCountry);
        setCountryByISOCode(isoForCountry);
    }

    /**
     * Gets the province of the current user.
     * @return the province of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setProvince(String)
     */
    public String getProvince() throws SkypeException {
        return getProperty("PROVINCE");
    }

    /**
     * Sets the province of the current user.
     * @param newValue the new province of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getProvince()
     */
    public void setProvince(final String newValue) throws SkypeException {
        setProperty("PROVINCE", newValue);
    }

    /**
     * Gets the city of the current user.
     * @return the city of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setCity(String)
     */
    public String getCity() throws SkypeException {
        return getProperty("CITY");
    }

    /**
     * Sets the city of the current user.
     * @param newValue the new city of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getCity()
     */
    public void setCity(final String newValue) throws SkypeException {
        setProperty("CITY", newValue);
    }

    /**
     * Gets the home phone number of the current user.
     * @return the home phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setHomePhoneNumber(String)
     */
    public String getHomePhoneNumber() throws SkypeException {
        return getProperty("PHONE_HOME");
    }

    /**
     * Sets the home phone number of the current user.
     * @param newValue the new home phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getHomePhoneNumber()
     */
    public void setHomePhoneNumber(final String newValue) throws SkypeException {
        setProperty("PHONE_HOME", newValue);
    }

    /**
     * Gets the office phone number of the current user.
     * @return the office phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setOfficePhoneNumber(String)
     */
    public String getOfficePhoneNumber() throws SkypeException {
        return getProperty("PHONE_OFFICE");
    }

    /**
     * Sets the office phone number of the current user.
     * @param newValue the new office phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getOfficePhoneNumber()
     */
    public void setOfficePhoneNumber(final String newValue) throws SkypeException {
        setProperty("PHONE_OFFICE", newValue);
    }

    /**
     * Gets the mobile phone number of the current user.
     * @return the mobile phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setMobilePhoneNumber(String)
     */
    public String getMobilePhoneNumber() throws SkypeException {
        return getProperty("PHONE_MOBILE");
    }

    /**
     * Sets the mobile phone number of the current user.
     * @param newValue the new mobile phone number of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getMobilePhoneNumber()
     */
    public void setMobilePhoneNumber(final String newValue) throws SkypeException {
        setProperty("PHONE_MOBILE", newValue);
    }

    /**
     * Gets the home page address of the current user.
     * @return the home page address of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setHomePageAddress(String)
     */
    @Deprecated
    public String getHomePageAddress() throws SkypeException {
        return getProperty("HOMEPAGE");
    }

    /**
     * Gets the web site address of the current user.
     * @return the web site address of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setWebSiteAddress(String)
     */
    public String getWebSiteAddress() throws SkypeException {
        return getHomePageAddress();
    }

    /**
     * Sets the home page address of the current user.
     * @param newValue the new home page address of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getHomePageAddress()
     */
    @Deprecated
    public void setHomePageAddress(final String newValue) throws SkypeException {
        setProperty("HOMEPAGE", newValue);
    }

    /**
     * Sets the web site address of the current user.
     * @param newValue the new web site address of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getWebSiteAddress()
     */
    public void setWebSiteAddress(final String newValue) throws SkypeException {
        setHomePageAddress(newValue);
    }

    /**
     * Gets the introduction of the current user.
     * @return the introduction of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setIntroduction(String)
     */
    public String getIntroduction() throws SkypeException {
        return getProperty("ABOUT");
    }

    /**
     * Sets the introduction of the current user.
     * @param newValue the new introduction of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getIntroduction()
     */
    public void setIntroduction(final String newValue) throws SkypeException {
        setProperty("ABOUT", newValue);
    }

    /**
     * Gets the mood message of the current user.
     * @return the mood message of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setMoodMessage(String)
     */
    public String getMoodMessage() throws SkypeException {
        return getProperty("MOOD_TEXT");
    }

    /**
     * Sets the mood message of the current user.
     * @param newValue the new mood message of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getMoodMessage()
     */
    public void setMoodMessage(final String newValue) throws SkypeException {
        setProperty("MOOD_TEXT", newValue);
    }

    /**
     * Gets the rich mood message of the current user.
     * @return the rich mood message of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     * @see #setRichMoodMessage(String)
     */
    public String getRichMoodMessage() throws SkypeException {
        return getProperty("RICH_MOOD_TEXT");
    }

    /**
     * Sets the rich mood message of the current user.
     * @param newValue the new rich mood message of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     * @see #getRichMoodMessage()
     */
    public void setRichMoodMessage(final String newValue) throws SkypeException {
        setProperty("RICH_MOOD_TEXT", newValue);
    }
    
    /**
     * Sets the avatar of the current user.
     * @param newValue the new image of the avatar.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     * @see #setAvatarByFile(File)
     * @see #getAvatar()
     */
    public void setAvatar(final BufferedImage newValue) throws SkypeException {
        if (newValue == null) {
            setAvatarByFile((File)null);
            return;
        }
        try {
            final File file = Utils.createTempraryFile("set_avator_", "jpg");
            if (ImageIO.write(newValue, "jpg", file)) {
                setAvatarByFile(file);
                file.delete();
            }
        } catch(IOException e) {
        }
    }

    /**
     * Sets the avatar of the current user by a file .
     * @param newValue the new image file of the avatar.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     * @see #setAvatar(BufferedImage)
     * @see #getAvatar()
     */
    public void setAvatarByFile(final File newValue) throws SkypeException {
        final String newValueString;
        if (newValue == null) {
            newValueString = "";
        } else {
            newValueString = newValue.getAbsolutePath();
        }
        Utils.setProperty("AVATAR", "1", newValueString);
    }
    
    /**
     * Gets the avatar of the current user.
     * @return the avatar image of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @since Protocol 7
     * @see #setAvatar(BufferedImage)
     * @see #setAvatarByFile(File)
     */
    public BufferedImage getAvatar() throws SkypeException {
        try {
            final File file = Utils.createTempraryFile("get_avator_", "jpg");
            final String command = "GET AVATAR 1 " + file.getAbsolutePath();
            final String responseHeader = "AVATAR 1 ";
            final String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
            final BufferedImage image = ImageIO.read(file);
            file.delete();
            return image;
        } catch(ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        } catch(IOException e) {
            return null;
        }
    }

    /**
     * Gets the time zone of the current user.
     * @return the time zone of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setTimeZone(int)
     */
    public int getTimeZone() throws SkypeException {
        return Integer.parseInt(getProperty("TIMEZONE"));
    }

    /**
     * Sets the time zone of the current user.
     * @param newValue the new time zone of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getTimeZone()
     */
    public void setTimeZone(final int newValue) throws SkypeException {
        setProperty("TIMEZONE", "" + newValue);
    }

    /**
     * Indicates whether the current user has a web camera.
     * @return <code>true</code> if the current user has a web camera; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public boolean isVideoCapable() throws SkypeException {
        return Boolean.parseBoolean(getProperty("IS_VIDEO_CAPABLE"));
    }

    /**
     * Gets the wait time in seconds before starting a call forwarding.
     * @return the wait time in seconds before starting a call forwarding.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setWaitTimeBeforeCallForwarding(int)
     */
    public int getWaitTimeBeforeCallForwarding() throws SkypeException {
        return Integer.parseInt(getProperty("CALL_NOANSWER_TIMEOUT"));
    }

    /**
     * Sets the wait time in seconds before starting a call forwarding.
     * @param newValue the new wait time in seconds before starting a call forwarding.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getWaitTimeBeforeCallForwarding()
     */
    public void setWaitTimeBeforeCallForwarding(final int newValue) throws SkypeException {
        setProperty("CALL_NOANSWER_TIMEOUT", "" + newValue);
    }

    /**
     * Indicates whether the call forwarding function is on.
     * @return <code>true</code> if the call forwarding function is on; <code>false</code> otherwise.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setCallForwarding(boolean)
     */
    public boolean isCallForwarding() throws SkypeException {
        return Boolean.parseBoolean(getProperty("CALL_APPLY_CF"));
    }

    /**
     * Starts or stops the call forwarding function.
     * @param on if <code>true</code>, starts the call forwarding function; otherwise, stops the call forwarding function
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #isCallForwarding()
     */
    public void setCallForwarding(final boolean on) throws SkypeException {
        setProperty("CALL_APPLY_CF", ("" + on).toUpperCase());
    }

    /**
     * Gets the all call forwarding rules of the current user.
     * @return the all call forwarding rules of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #setAllCallForwardingRules(CallForwardingRule[])
     */
    public CallForwardingRule[] getAllCallForwardingRules() throws SkypeException {
        List<CallForwardingRule> rules = new ArrayList<CallForwardingRule>();
        String value = getProperty("CALL_FORWARD_RULES");
        if ("".equals(value)) {
            return new CallForwardingRule[0];
        }
        for (String rule : value.split(" ")) {
            String[] elements = rule.split(",");
            rules.add(new CallForwardingRule(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]), elements[2]));
        }
        return rules.toArray(new CallForwardingRule[0]);
    }

    /**
     * Sets the all call forwarding rules of the current user.
     * @param newValues the new all call forwarding rules of the current user.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     * @see #getAllCallForwardingRules()
     */
    public void setAllCallForwardingRules(CallForwardingRule[] newValues) throws SkypeException {
        if (newValues == null) {
            newValues = new CallForwardingRule[0];
        }
        setProperty("CALL_FORWARD_RULES", toSpaceSeparatedString(newValues));
    }

    /**
     * Return all the valid SMS numbers.
     * @return Array of Strings with the numbers.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    public String[] getAllValidSMSNumbers() throws SkypeException {
        return Utils.convertToArray(getProperty("SMS_VALIDATED_NUMBERS"));
    }

    /**
     * Return the value of a property for PROFILE object.
     * @param name name of the parameter.
     * @return the value of the parameter.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    private String getProperty(final String name) throws SkypeException {
        return Utils.getProperty("PROFILE", name);
    }

    /**
     * Set a value for a PROFILE property.
     * @param name name of the property.
     * @param value value of the property.
     * @throws SkypeException when the connection has gone bad or an ERROR message is received.
     */
    private void setProperty(final String name, final String value) throws SkypeException {
        Utils.setProperty("PROFILE", name, Utils.convertNullToEmptyString(value));
    }
    
    /**
     * Adds a PropertyChangeListener to this user.
     * <p>
     * The listener is registered for all bound properties of this user, including the following:
     * <ul>
     *    <li>this user's status ("status")</li>
     *    <li>this user's mood message ("moodMessage")</li>
     * </ul>
     * </p><p>
     * If listener is null, no exception is thrown and no action is performed.
     * </p>
     * @param listener the PropertyChangeListener to be added
     * @see #removePropertyChangeListener(PropertyChangeListener)
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) throws SkypeException {
        synchronized (propertyChangeListenerMutex) {
            if (propertyChangeListener == null) {
                ConnectorListener connectorListener = new AbstractConnectorListener() {
                    @Override
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("PROFILE ")) {
                            String data = message.substring("PROFILE ".length());
                            String propertyName = data.substring(0, data.indexOf(' '));
                            String propertyValue = data.substring(data.indexOf(' ') + 1);
                            if (propertyName.equals("MOOD_TEXT")) {
                                firePropertyChanged(MOOD_TEXT_PROPERTY, null, propertyValue);
                            }
                        } else if (message.startsWith("USERSTATUS ")) {
                            String value = message.substring("USERSTATUS ".length());
                            firePropertyChanged(STATUS_PROPERTY, null, Status.valueOf(value));
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(connectorListener);
                    propertyChangeListener = connectorListener;
                } catch(ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
        listeners.addPropertyChangeListener(listener);
    }
    
    private void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        listeners.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /**
     * Removes the PropertyChangeListener from this user.
     * <p>
     * If listener is null, no exception is thrown and no action is performed.
     * </p>
     * @param listener the PropertyChangeListener to be removed
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
}
