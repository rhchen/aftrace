/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package net.sf.nexuslite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLongLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * Nexus trace type
 *
 * @author Matthew Khouzam
 */
public class NexusTrace extends TmfTrace implements ITmfEventParser {

    public static final String PLUGIN_ID = "net.sf.nexuslite";
	
	private static final int CHUNK_SIZE = 65536; // seems fast on MY system
    private static final int EVENT_SIZE = 8; // according to spec

    private TmfLongLocation fCurrentLocation;
    private static final TmfLongLocation NULLLOCATION = new TmfLongLocation(
            (Long) null);
    private static final TmfContext NULLCONTEXT = new TmfContext(NULLLOCATION,
            -1L);

    private long fSize;
    private long fOffset;
    private File fFile;
    private String[] fEventTypes;
    private FileChannel fFileChannel;
    private MappedByteBuffer fMappedByteBuffer;

    @Override
    public IStatus validate(@SuppressWarnings("unused") IProject project,
            String path) {
        File f = new File(path);
        if (!f.exists()) {
            return new Status(IStatus.ERROR, PLUGIN_ID,
                    "File does not exist"); //$NON-NLS-1$
        }
        if (!f.isFile()) {
            return new Status(IStatus.ERROR, PLUGIN_ID, path
                    + " is not a file"); //$NON-NLS-1$
        }
        String header = readHeader(f);
        if (header.split(",", 64).length == 64) { //$NON-NLS-1$
            return Status.OK_STATUS;
        }
        return new Status(IStatus.ERROR, PLUGIN_ID,
                "File does not start as a CSV"); //$NON-NLS-1$
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return fCurrentLocation;
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        fFile = new File(path);
        fSize = fFile.length();
        if (fSize == 0) {
            throw new TmfTraceException("file is empty"); //$NON-NLS-1$
        }
        String header = readHeader(fFile);
        if (header == null) {
            throw new TmfTraceException("File does not start as a CSV"); //$NON-NLS-1$
        }
        fEventTypes = header.split(",", 64); // 64 values of types according to //$NON-NLS-1$
                                             // the 'spec'
        if (fEventTypes.length != 64) {
            throw new TmfTraceException(
                    "Trace header does not contain 64 event names"); //$NON-NLS-1$
        }
        if (getNbEvents() < 1) {
            throw new TmfTraceException("Trace does not have any events"); //$NON-NLS-1$
        }
        try {
            fFileChannel = new FileInputStream(fFile).getChannel();
            seek(0);
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage());
        }
    }

    /**
     * @return
     */
    private String readHeader(File file) {
        String header = new String();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            header = br.readLine();
            br.close();
        } catch (IOException e) {
            return null;
        }
        fOffset = header.length() + 1;
        setNbEvents((fSize - fOffset) / EVENT_SIZE);
        return header;
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        return ((TmfLongLocation) location).getLocationInfo().doubleValue()
                / getNbEvents();
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        TmfLongLocation nl = (TmfLongLocation) location;
        if (location == null) {
            nl = new TmfLongLocation(0L);
        }
        try {
            seek(nl.getLocationInfo());
        } catch (IOException e) {
            return NULLCONTEXT;
        }
        return new TmfContext(nl, nl.getLocationInfo());
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        long rank = (long) (ratio * getNbEvents());
        try {
            seek(rank);
        } catch (IOException e) {
            return NULLCONTEXT;
        }
        return new TmfContext(new TmfLongLocation(rank), rank);
    }

    private void seek(long rank) throws IOException {
        final long position = fOffset + (rank * EVENT_SIZE);
        int size = Math.min((int) (fFileChannel.size() - position), CHUNK_SIZE);
        fMappedByteBuffer = fFileChannel.map(MapMode.READ_ONLY, position, size);
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        if ((context == null) || (context.getRank() == -1)) {
            return null;
        }
        TmfEvent event = null;
        long ts = -1;
        int type = -1;
        int payload = -1;
        long pos = context.getRank();
        if (pos < getNbEvents()) {
            try {
                // if we are approaching the limit size, move to a new window
                if ((fMappedByteBuffer.position() + EVENT_SIZE) > fMappedByteBuffer
                        .limit()) {
                    seek(context.getRank());
                }
                /*
                 * the trace format, is:
                 *
                 * - 32 bits for the time,
                 * - 6 for the event type,
                 * - 26 for the data.
                 *
                 * all the 0x00 stuff are masks.
                 */

                /*
                 * it may be interesting to assume if the ts goes back in time,
                 * it actually is rolling over we would need to keep the
                 * previous timestamp for that, keep the high bits and increment
                 * them if the next int ts read is lesser than the previous one
                 */

                ts = 0x00000000ffffffffL & fMappedByteBuffer.getInt();

                long data = 0x00000000ffffffffL & fMappedByteBuffer.getInt();
                type = (int) (data >> 26) & (0x03f); // first 6 bits
                payload = (int) (data & 0x003FFFFFFL); // last 26 bits
                // the time is in microseconds.
                TmfTimestamp timestamp = new TmfTimestamp(ts, ITmfTimestamp.MICROSECOND_SCALE);
                final String title = fEventTypes[type];
                // put the value in a field
                final TmfEventField tmfEventField = new TmfEventField(
                        "value", payload, null); //$NON-NLS-1$
                // the field must be in an array
                final TmfEventField[] fields = new TmfEventField[1];
                fields[0] = tmfEventField;
                final TmfEventField content = new TmfEventField(
                        ITmfEventField.ROOT_FIELD_ID, null, fields);
                // set the current location

                fCurrentLocation = new TmfLongLocation(pos);
                // create the event
                event = new TmfEvent(this, pos, timestamp, null,
                        new TmfEventType(title, title, null), content, null);
            } catch (IOException e) {
                fCurrentLocation = new TmfLongLocation(-1L);
            }
        }
        return event;
    }
}