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

/**
 * This exception is used for commands that get a ERROR reply.
 * @author Koji Hisano
 */
public final class CommandFailedException extends SkypeException {
    /**
	 * serialVersionUID needed for all serialisation objects.
	 */
	private static final long serialVersionUID = 5247715297475793607L;

	/**
	 * ERROR code refrence.
	 * @see https://developer.skype.com/Docs/ApiDoc/Error_codes
	 */
	private int code;

	/**
	 * The error message.
	 */
    private String message;

    /**
     * Constructor with parsing.
     * @param response the complete ERROR string.
     */
    CommandFailedException(String response) {
        super(response);
        if (response.startsWith("ERROR ")) {
            response = response.substring("ERROR ".length());
        }
        int spaceIndex = response.indexOf(' ');
        if (spaceIndex == -1) {
        	code = Integer.parseInt(response);
        	message = "ERROR " + response;
        }
        else {
        	code = Integer.parseInt(response.substring(0, spaceIndex));
        	message = response.substring(spaceIndex + 1);
        }
    }

    /**
     * returns the error code.
     * @return error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the humanreadible error message.
     * @return message.
     */
    public String getMessage() {
        return message;
    }
}
