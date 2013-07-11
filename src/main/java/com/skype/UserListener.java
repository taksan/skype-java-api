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

/**
 * Listener interface for USER objects status changed events.
 * @see User
 * @author Fabio D. C. Depin
 */
public interface UserListener {
    /**
     * Called when the status of a USER object changes.
     * @param STATUS identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void statusMonitor(User.Status status, User user) throws SkypeException;

    /**
     * Called when the mood text of a USER object changes.
     * @param moodText identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void moodTextMonitor(String moodText, User user) throws SkypeException;

    /**
     * Called when the full name of a USER object changes.
     * @param fullName identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void fullNameMonitor(String fullName, User user) throws SkypeException;

    /**
     * Called when the phone mobile of a USER object changes.
     * @param phoneMobile identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void phoneMobileMonitor(String phoneMobile, User user) throws SkypeException;

    /**
     * Called when the phone home of a USER object changes.
     * @param phoneHome identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void phoneHomeMonitor(String phoneHome, User user) throws SkypeException;

    /**
     * Called when the phone office of a USER object changes.
     * @param phoneOffice identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void phoneOfficeMonitor(String phoneOffice, User user) throws SkypeException;

    /**
     * Called when the display name of a USER object changes.
     * @param displayName identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void displayNameMonitor(String displayName, User user) throws SkypeException;

    /**
     * Called when the country of a USER object changes.
     * @param country identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void countryMonitor(String country, User user) throws SkypeException;

    /**
     * Called when the province of a USER object changes.
     * @param province identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void provinceMonitor(String province, User user) throws SkypeException;

    /**
     * Called when the city of a USER object changes.
     * @param city identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void cityMonitor(String city, User user) throws SkypeException;

    /**
     * Called when the time zone of a USER object changes.
     * @param timeZone identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void timeZoneMonitor(String timeZone, User user) throws SkypeException;

    /**
     * Called when the sex of a USER object changes.
     * @param sex identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void sexMonitor(User.Sex sex, User user) throws SkypeException;

    /**
     * Called when the home page of a USER object changes.
     * @param homePage identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void homePageMonitor(String homePage, User user) throws SkypeException;

    /**
     * Called when the birthday of a USER object changes.
     * @param birthday identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void birthdayMonitor(String birthday, User user) throws SkypeException;

    /**
     * Called when the language of a USER object changes.
     * @param language identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void languageMonitor(String language, User user) throws SkypeException;

    /**
     * Called when the about of a USER object changes.
     * @param about identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void aboutMonitor(String about, User user) throws SkypeException;

    /**
     * Called when the blocked of a USER object changes.
     * @param isBlocked identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void isBlockedMonitor(boolean isBlocked, User user) throws SkypeException;

    /**
     * Called when the authorized of a USER object changes.
     * @param isAuthorized identified that changed.
     * @param USER object.
     * @throws SkypeException when a connection is gone bad.
     */
    void isAuthorizedMonitor(boolean isAuthorized, User user) throws SkypeException;
}
