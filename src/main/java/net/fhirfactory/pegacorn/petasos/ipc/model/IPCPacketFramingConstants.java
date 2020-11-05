/*
 * Copyright (c) 2020 Mark A. Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.petasos.ipc.model;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IPCPacketFramingConstants {
    private static final Integer IPC_PACKET_MAXIMUM_FRAME_SIZE = 26214400;
    private static final String IPC_PACKET_FRAME_END = "<|><ETX><|>";
    private static final String IPC_PACKET_FRAME_START = "<|><STX><|>";

    public static Integer getIpcPacketMaximumFrameSize() {
        return IPC_PACKET_MAXIMUM_FRAME_SIZE;
    }

    public static String getIpcPacketFrameEnd() {
        return IPC_PACKET_FRAME_END;
    }

    public static String getIpcPacketFrameStart() {
        return IPC_PACKET_FRAME_START;
    }
}
