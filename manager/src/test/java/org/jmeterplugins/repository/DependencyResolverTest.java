package org.jmeterplugins.repository;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class DependencyResolverTest {
    @Test
    public void testSimpleInstall() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();
        PluginMock install = new PluginMock("install", null);
        Map<String, String> libs = new HashMap<>();
        libs.put("test", "test");
        libs.put("jorphan", "test");
        install.setLibs(libs);
        plugs.put(install, true);
        PluginMock uninstall = new PluginMock("uninstall", "1.0");
        plugs.put(uninstall, false);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();
        Map<String, String> libAdds = obj.getLibAdditions();

        assertEquals(1, adds.size());
        assertEquals(1, dels.size());
        assertEquals(1, libAdds.size());
        assertTrue(adds.contains(install));
        assertTrue(dels.contains(uninstall));
    }

    @Test
    public void testUpgrade() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();
        PluginMock upgrade = new PluginMock("install", "1.0");
        upgrade.setCandidateVersion("0.1");
        plugs.put(upgrade, true);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();

        assertEquals(1, adds.size());
        assertEquals(1, dels.size());
        assertTrue(adds.contains(upgrade));
        assertTrue(dels.contains(upgrade));
    }

    @Test
    public void testDepInstall() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();
        PluginMock jdbc = new PluginMock("jdbc", null);
        plugs.put(jdbc, false);
        PluginMock http = new PluginMock("http", null);
        plugs.put(http, false);
        PluginMock components = new PluginMock("components", null);
        plugs.put(components, false);

        PluginMock standard = new PluginMock("standard", null);
        HashSet<String> depsStandard = new HashSet<>();
        depsStandard.add(http.getID());
        depsStandard.add(components.getID());
        standard.setDepends(depsStandard);
        plugs.put(standard, false);

        PluginMock extras = new PluginMock("extras", null);
        HashSet<String> depsExtras = new HashSet<>();
        depsExtras.add(standard.getID());
        depsExtras.add(jdbc.getID());
        depsExtras.add(http.getID());
        extras.setDepends(depsExtras);
        plugs.put(extras, true);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();

        assertEquals(5, adds.size());
        assertEquals(0, dels.size());
        assertTrue(adds.contains(jdbc));
        assertTrue(adds.contains(components));
        assertTrue(adds.contains(standard));
        assertTrue(adds.contains(extras));
        assertTrue(adds.contains(http));
    }

    @Test
    public void testDepInstallJMeterHTTP() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();

        PluginMock cause = new PluginMock("cause", Plugin.getJMeterVersion());
        cause.setVersions(JSONObject.fromObject("{\"\":null}", new JsonConfig()));
        plugs.put(cause, true);

        PluginMock effect = new PluginMock("effect", null);
        HashSet<String> deps = new HashSet<>();
        deps.add(cause.getID());
        effect.setDepends(deps);
        plugs.put(effect, true);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();

        assertTrue(adds.contains(effect));
        assertFalse(adds.contains(cause));
        assertEquals(1, adds.size());
        assertEquals(0, dels.size());
    }

    @Test
    public void testDepUninstall() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();
        PluginMock a = new PluginMock("root", "1.0");
        plugs.put(a, false);

        PluginMock b = new PluginMock("cause", "1.0");
        HashSet<String> deps = new HashSet<>();
        deps.add(a.getID());
        b.setDepends(deps);
        plugs.put(b, true);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();

        assertEquals(0, adds.size());
        assertEquals(2, dels.size());
        assertTrue(dels.contains(a));
        assertTrue(dels.contains(b));
    }

    @Test
    public void testDepLibUninstall() throws Exception {
        Map<Plugin, Boolean> plugs = new HashMap<>();

        PluginMock a = new PluginMock("a", "1.0");
        Map<String, String> aLibs = new HashMap<>();
        aLibs.put("aaa", "");
        aLibs.put("guava", "");
        aLibs.put("bbb", "");
        a.setLibs(aLibs);
        plugs.put(a, false);

        PluginMock b = new PluginMock("b", null);
        Map<String, String> bLibs = new HashMap<>();
        bLibs.put("aa", "");
        bLibs.put("guava", "");
        bLibs.put("bb", "");
        b.setLibs(bLibs);
        plugs.put(b, true);

        DependencyResolver obj = new DependencyResolver(plugs);
        Set<Plugin> adds = obj.getAdditions();
        Set<Plugin> dels = obj.getDeletions();

        assertTrue(!obj.getLibDeletions().contains("guava"));
        assertEquals(1, adds.size());
        assertEquals(1, dels.size());
        assertTrue(dels.contains(a));
        assertTrue(adds.contains(b));
    }
}