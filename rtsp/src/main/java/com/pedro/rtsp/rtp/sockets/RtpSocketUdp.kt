/*
 * Copyright (C) 2021 pedroSG94.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedro.rtsp.rtp.sockets

import android.util.Log
import com.pedro.rtsp.rtsp.RtpFrame
import com.pedro.rtsp.utils.RtpConstants
import java.io.IOException
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.UnknownHostException

/**
 * Created by pedro on 7/11/18.
 */
class RtpSocketUdp(videoSourcePort: Int, audioSourcePort: Int) : BaseRtpSocket() {

  private var multicastSocketVideo: MulticastSocket? = null
  private var multicastSocketAudio: MulticastSocket? = null
  private val datagramPacket = DatagramPacket(byteArrayOf(0), 1)

  init {
    multicastSocketVideo = MulticastSocket(videoSourcePort)
    multicastSocketVideo?.timeToLive = 64
    multicastSocketAudio = MulticastSocket(audioSourcePort)
    multicastSocketAudio?.timeToLive = 64
  }

  @Throws(IOException::class)
  override fun setDataStream(outputStream: OutputStream, host: String) {
    datagramPacket.address = InetAddress.getByName(host)
  }

  @Throws(IOException::class)
  override suspend fun sendFrame(rtpFrame: RtpFrame, isEnableLogs: Boolean) {
    sendFrameUDP(rtpFrame, isEnableLogs)
  }

  override fun close() {
    multicastSocketVideo?.close()
    multicastSocketAudio?.close()
  }

  @Throws(IOException::class)
  private fun sendFrameUDP(rtpFrame: RtpFrame, isEnableLogs: Boolean) {
    synchronized(RtpConstants.lock) {
      datagramPacket.data = rtpFrame.buffer
      datagramPacket.port = rtpFrame.rtpPort
      datagramPacket.length = rtpFrame.length
      if (rtpFrame.isVideoFrame()) {
        multicastSocketVideo?.send(datagramPacket)
      } else {
        multicastSocketAudio?.send(datagramPacket)
      }
      if (isEnableLogs) {
        Log.i(TAG, "wrote packet: ${(if (rtpFrame.isVideoFrame()) "Video" else "Audio")}, size: ${rtpFrame.length}, port: ${rtpFrame.rtpPort}")
      }
    }
  }
}