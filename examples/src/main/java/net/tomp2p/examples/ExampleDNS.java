/*
 * Copyright 2011 Thomas Bocek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.tomp2p.examples;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
 * See http://tomp2p.net/doc/quick/ for more information on this
 * 
 * start with arguments "1 test 127.0.0.1" for the server and "2 test" for the client 
 */
public class ExampleDNS {
	final private PeerDHT peer;

	public ExampleDNS(int nodeId) throws Exception {
		peer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(nodeId)).ports(4000 + nodeId).start()).start();
		FutureBootstrap fb = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName("127.0.0.1")).ports(4001).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		ExampleDNS dns = new ExampleDNS(Integer.parseInt(args[0]));
		if (args.length == 3) {
			dns.store(args[1], args[2]);
		}
		if (args.length == 2) {
			System.out.println("Name:" + args[1] + " IP:" + dns.get(args[1]));
		}
	}

	private String get(String name) throws ClassNotFoundException, IOException {
		FutureGet futureGet = peer.get(Number160.createHash(name)).start();
		futureGet.awaitUninterruptibly();
		if (futureGet.isSuccess()) {
			return futureGet.dataMap().values().iterator().next().object().toString();
		}
		return "not found";
	}

	private void store(String name, String ip) throws IOException {
		peer.put(Number160.createHash(name)).data(new Data(ip)).start().awaitUninterruptibly();
	}
}
