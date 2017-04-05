package net.tomp2p.dht;

import net.tomp2p.peers.Number160;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestStorageMemoryReplication {
	
	@Rule
    public TestRule watcher = new TestWatcher() {
	   protected void starting(Description description) {
          System.out.println("Starting test: " + description.getMethodName());
       }
    };

	@Test
	public void testStorageMemoryReplication1() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("test1");
		Number160 testPer = Number160.createHash("test2");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		Assert.assertEquals(testPer, storageMemoryReplication.findPeerIDsForResponsibleContent(testLoc));
	}

	@Test
	public void testStorageMemoryReplication2() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("loc1");
		Number160 testPer1 = Number160.createHash("peer1");
		Number160 testPer2 = Number160.createHash("peer2");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer1);
		storageMemoryReplication.updateResponsibilities(testLoc, testPer2);
		Assert.assertFalse(storageMemoryReplication.updateResponsibilities(testLoc, testPer2));
		Assert.assertTrue(storageMemoryReplication.updateResponsibilities(testLoc, testPer1));
	}

	@Test
	public void testStorageMemoryReplication3() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("test1");
		Number160 testPer = Number160.createHash("test2");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		Assert.assertEquals(testLoc, storageMemoryReplication.findContentForResponsiblePeerID(testPer)
				.iterator().next());
	}

	@Test
	public void testStorageMemoryReplication4() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc1 = Number160.createHash("loc1");
		Number160 testLoc2 = Number160.createHash("loc2");
		Number160 testPer = Number160.createHash("peer1");
		storageMemoryReplication.updateResponsibilities(testLoc1, testPer);
		storageMemoryReplication.updateResponsibilities(testLoc2, testPer);
		Assert.assertEquals(2, storageMemoryReplication.findContentForResponsiblePeerID(testPer).size());
	}

	@Test
	public void testStorageMemoryReplication5() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("test1");
		Number160 testPer = Number160.createHash("test2");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		Assert.assertEquals(testPer, storageMemoryReplication.findPeerIDsForResponsibleContent(testLoc));
		Assert.assertEquals(testLoc, storageMemoryReplication.findContentForResponsiblePeerID(testPer)
				.iterator().next());
	}

	@Test
	public void testStorageMemoryReplication6() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("test1");
		Number160 testPer = Number160.createHash("test2");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		storageMemoryReplication.updateResponsibilities(testLoc, testPer);
		storageMemoryReplication.removeResponsibility(testLoc);
		Assert.assertEquals(null, storageMemoryReplication.findPeerIDsForResponsibleContent(testLoc));
	}

	@Test
	public void testStorageMemoryReplication7() {
		StorageMemory storageMemoryReplication = new StorageMemory();
		Number160 testLoc = Number160.createHash("test1");
		Number160 testPer1 = Number160.createHash("test2");
		Number160 testPer2 = Number160.createHash("test3");
		storageMemoryReplication.updateResponsibilities(testLoc, testPer1);
		storageMemoryReplication.updateResponsibilities(testLoc, testPer2);
		storageMemoryReplication.removeResponsibility(testLoc);
		Assert.assertEquals(null, storageMemoryReplication.findPeerIDsForResponsibleContent(testLoc));
		Assert.assertEquals(null, storageMemoryReplication.findContentForResponsiblePeerID(testPer1));
		Assert.assertEquals(null, storageMemoryReplication.findContentForResponsiblePeerID(testPer2));
	}
}
