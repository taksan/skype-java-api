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
package com.skype.connector.test;

import java.util.*;

import com.skype.connector.*;

public final class TestConnector extends Connector {
    private static class Holder {
        static final TestConnector instance = new TestConnector();
    }

    public static TestConnector getInstance() {
        // http://en.wikipedia.org/wiki/Initialization_on_Demand_Holder_Idiom
        return Holder.instance;
    }

    private Object recordingFieldsMutex = new Object();
    private ConnectorListener recordListener;
    private List<Recorder> recorderList;
    private Recorder[] recorders = new Recorder[0];

    private Object playingFieldsMutex = new Object();
    private Player player;
    private ConnectorListener playingLister;
    private Thread playerThread;
    private Object sentMessageFieldsMutex = new Object();
    private String sentMessage = null;

    private TestConnector() {
    }

    public void addRecorder(final Recorder recorder) throws ConnectorException {
        synchronized(recordingFieldsMutex) {
            Connector.getInstance().connect();
            if(recordListener == null) {
                recordListener = new AbstractConnectorListener() {
                    @Override
                    public void messageSent(ConnectorMessageEvent event) {
                        long time = System.currentTimeMillis();
                        Recorder[] recorders = TestConnector.this.recorders;
                        for(Recorder recorder: recorders) {
                            recorder.recordSentMessage(time - recorder.getStartTime(), event.getMessage());
                        }
                    }

                    @Override
                    public void messageReceived(ConnectorMessageEvent event) {
                        long time = System.currentTimeMillis();
                        Recorder[] recorders = TestConnector.this.recorders;
                        for(Recorder recorder: recorders) {
                            recorder.recordReceivedMessage(time - recorder.getStartTime(), event.getMessage());
                        }
                    }
                };
                Connector.getInstance().addConnectorListener(recordListener, false, true);
            }
            if(recorderList == null) {
                recorderList = new ArrayList<Recorder>();
            }
            if(!recorderList.contains(recorder)) {
                recorderList.add(recorder);
                recorders = recorderList.toArray(new Recorder[0]);
                if(!recorder.isStarted()) {
                    recorder.setStartTime(System.currentTimeMillis());
                }
            }
        }
    }

    public void removeRecorder(final Recorder recorder) throws ConnectorException {
        synchronized(recordingFieldsMutex) {
            if(!recorderList.contains(recorder)) {
                return;
            }
            recorderList.remove(recorder);
            recorders = recorderList.toArray(new Recorder[0]);
            if(recorderList.isEmpty()) {
                recorderList = null;
                Connector.getInstance().removeConnectorListener(recordListener);
                recordListener = null;
            }
        }
    }

    public void setPlayer(final Player player) throws ConnectorException {
        synchronized(playingFieldsMutex) {
            Connector.setInstance(this);
            Connector.getInstance().connect();
            if(this.player != null) {
                clearPlayer();
            }
            this.player = player;
            playingLister = new AbstractConnectorListener() {
                @Override
                public void messageSent(ConnectorMessageEvent event) {
                    // TODO Check the below code when synchronous event mechanism is changed
                    if (event.getMessage().startsWith("PROTOCOL ")) {
                        return;
                    }

                    synchronized(sentMessageFieldsMutex) {
                        while (sentMessage != null) {
                            try {
                                sentMessageFieldsMutex.wait();
                            } catch(InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        sentMessage = event.getMessage();
                        sentMessageFieldsMutex.notify();
                    }
                }
            };
            Connector.getInstance().addConnectorListener(playingLister, false, true);
            playerThread = new Thread("TestConnectorPlayer") {
                @Override
                public void run() {
                    try {
                        player.init();
                        long startTime = System.currentTimeMillis();
                        while (player.hasNextMessage()) {
                            PlayerMessage message = player.getNextMessage();
                            switch (message.getType()) {
                                case SENT:
                                    synchronized(sentMessageFieldsMutex) {
                                        while (sentMessage == null) {
                                            try {
                                                sentMessageFieldsMutex.wait();
                                            } catch(InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                return;
                                            }
                                        }
                                        String sentMessage = TestConnector.this.sentMessage;
                                        TestConnector.this.sentMessage = null;
                                        sentMessageFieldsMutex.notify();
                                        if (!message.getMessage().equals(sentMessage)) {
                                            throw new IllegalStateException("The sent message (=\"" + sentMessage + "\")is not equal to the expected message (=\"" + message.getMessage() + "\").");
                                        }
                                    }
                                    break;
                                case RECEIVED:
                                    long period = System.currentTimeMillis() - startTime - message.getTime();
                                    if (period < 0) {
                                        try {
                                            Thread.sleep(-period);
                                        } catch(InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                            return;
                                        }
                                    }
                                    fireMessageReceived(message.getMessage());
                                    break;
                            }
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                        }
                    } finally {
                        player.destory();
                    }
                }
            };
            playerThread.setDaemon(true);
            playerThread.start();
        }
    }

    public void clearPlayer() throws ConnectorException {
        synchronized(playingFieldsMutex) {
            playerThread.interrupt();
            playerThread = null;
            Connector.getInstance().removeConnectorListener(playingLister);
            playingLister = null;
            player = null;
            Connector.setInstance(null);
        }
    }

    @Override
    protected void initializeImpl() throws ConnectorException {
        setStatus(Status.ATTACHED);
    }

    @Override
    protected Status connect(int timeout) throws ConnectorException {
        return getStatus();
    }

    @Override
    protected void sendCommand(String command) {
        if (command.equals("PROTOCOL 9999")) {
            fireMessageReceived("PROTOCOL 6");
        }
    }

    @Override
    protected void disposeImpl() {
    }
}
