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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.MessageProcessor;
import com.skype.connector.osx.OSXConnector;

public class SkypeTracer extends Shell {
    public static void main(final String args[]) throws Exception {
        OSXConnector.disableSkypeEventLoop();
        
        final Display display = Display.getDefault();
        SkypeTracer shell = new SkypeTracer(display, SWT.SHELL_TRIM);
        shell.layout();
        shell.open();
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    public SkypeTracer(Display display, int style) throws ConnectorException {
        super(display, style);
        createContents();
    }

    private void createContents() throws ConnectorException {
        setText("Skype Tracer");
        setSize(400, 300);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);

        final Text fromSkype = new Text(this, SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
        fromSkype.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
        new Thread() {
            public void run() {
                Connector.getInstance().setDebugOut(new PrintWriter(new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) throws IOException {
                        final String appended = new String(cbuf, off, len);
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                if (!fromSkype.isDisposed()) {
                                    fromSkype.append(appended);
                                }
                            }
                        });
                    }

                    @Override
                    public void flush() throws IOException {
                        // Do nothing
                    }

                    @Override
                    public void close() throws IOException {
                        // Do nothing
                    }
                }));
                try {
                    Connector.getInstance().setDebug(true);
                } catch(ConnectorException e) {
                }
            }
        }.start();

        final Text toSkype = new Text(this, SWT.BORDER);
        toSkype.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

        final Button send = new Button(this, SWT.NONE);
        send.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                final String command = toSkype.getText();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Connector.getInstance().execute(command, new MessageProcessor() {
                                @Override
                                protected void messageReceived(String message) {
                                    releaseLock();
                                }
                            });
                        } catch(ConnectorException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        send.setText("&Send");
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
