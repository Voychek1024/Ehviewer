/*
 * Copyright 2022 Tarsin Norbin
 *
 * This file is part of EhViewer
 *
 * EhViewer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * EhViewer is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EhViewer.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.hippo.ehviewer.client

import android.util.Log
import com.hippo.ehviewer.Settings
import com.hippo.ehviewer.builtInHosts
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val EXCEPTIONAL_DOMAIN = "hath.network"
private val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

object EhSSLSocketFactory : SSLSocketFactory() {
    override fun getDefaultCipherSuites(): Array<String> = sslSocketFactory.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = sslSocketFactory.supportedCipherSuites
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        val address = s.inetAddress.hostAddress.takeIf { host in builtInHosts || EXCEPTIONAL_DOMAIN in host || host in Settings.dohUrl }
        Log.d("EhSSLSocketFactory", "Host: $host Address: $address")
        return sslSocketFactory.createSocket(s, address ?: host, port, autoClose) as SSLSocket
    }
    override fun createSocket(host: String, port: Int): Socket = sslSocketFactory.createSocket(host, port)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket = sslSocketFactory.createSocket(host, port, localHost, localPort)
    override fun createSocket(host: InetAddress, port: Int): Socket = sslSocketFactory.createSocket(host, port)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket = sslSocketFactory.createSocket(address, port, localAddress, localPort)
}

fun OkHttpClient.Builder.install(sslSocketFactory: SSLSocketFactory) = apply {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())!!
    factory.init(null as KeyStore?)
    val manager = factory.trustManagers!!
    val trustManager = manager.filterIsInstance<X509TrustManager>().first()
    sslSocketFactory(sslSocketFactory, trustManager)
}
