/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.base;

import org.spf4j.os.OperatingSystem;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.spf4j.concurrent.DefaultExecutor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.io.ByteArrayBuilder;
import org.spf4j.jmx.JmxExport;
import org.spf4j.jmx.Registry;
import org.spf4j.os.ProcessHandler;
import org.spf4j.os.ProcessResponse;
import org.spf4j.recyclable.impl.ArraySuppliers;
import org.spf4j.stackmonitor.FastStackCollector;
import org.spf4j.unix.JVMArguments;
import org.spf4j.unix.Lsof;
import org.spf4j.unix.UnixRuntime;

/**
 *
 * @author zoly
 */
public final class Runtime {

  public static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
  public static final int WAIT_FOR_SHUTDOWN_MILLIS = Integer.getInteger("spf4j.waitForShutdownMillis", 30000);
  public static final String TMP_FOLDER = System.getProperty("java.io.tmpdir");
  public static final String JAVA_VERSION = System.getProperty("java.version");
  public static final String USER_NAME = System.getProperty("user.name");
  public static final String USER_DIR = System.getProperty("user.dir");
  public static final String USER_HOME = System.getProperty("user.home");
  public static final String JAVA_HOME = System.getProperty("java.home");
  private static final SortedMap<Integer, Set<Runnable>> SHUTDOWN_HOOKS = new TreeMap<>();
  public static final ThreadLocal<Long> DEADLINE = new ThreadLocal<Long>() {

    @Override
    protected Long initialValue() {
      return Long.MAX_VALUE;
    }
  };

  /**
   * unique identifier identifying this process.
   */
  public static final int PID;
  public static final String OS_NAME;
  public static final String PROCESS_NAME;
  public static final String PROCESS_ID;
  public static final int NR_PROCESSORS;
  private static final boolean IS_MAC_OSX;
  private static final boolean IS_WINDOWS;
  public static final Version JAVA_PLATFORM;
  private static final List<Class<?>> PRELOADED; // preload certain classes to have them available.

  static {
    // priming certain functionality to make sure it works when we need it (classes are already loaded).
    try (PrintStream stream = new PrintStream(new ByteArrayBuilder(), false, "UTF-8")) {
      Throwables.writeTo(new RuntimeException("priming"), stream, Throwables.PackageDetail.NONE);
    } catch (UnsupportedEncodingException ex) {
      throw new ExceptionInInitializerError(ex);
    }
    PRELOADED = new ArrayList<>(2);
    PRELOADED.add(FastStackCollector.class);
    final java.lang.Runtime runtime = java.lang.Runtime.getRuntime();
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    final int availableProcessors = runtime.availableProcessors();
    if (availableProcessors <= 0) {
      System.err.println("Invalid number of processors " + availableProcessors
              + " defaulting to 1");
      NR_PROCESSORS = 1;
    } else {
      NR_PROCESSORS = availableProcessors;
    }
    String mxBeanName = runtimeMxBean.getName();
    PROCESS_NAME = mxBeanName;
    PROCESS_ID = mxBeanName + ':' + Long.toHexString(System.currentTimeMillis());
    int atIdx = mxBeanName.indexOf('@');
    if (atIdx < 0) {
      PID = -1;
    } else {
      PID = Integer.parseInt(mxBeanName.substring(0, atIdx));
    }
    final String osName = System.getProperty("os.name");
    OS_NAME = osName;
    IS_MAC_OSX = "Mac OS X".equals(osName);
    IS_WINDOWS = osName.startsWith("Windows");
    runtime.addShutdownHook(new Thread(new AbstractRunnable(false) {
      @Override
      public void doRun() throws Exception {
        Exception rex = null;
        SortedMap<Integer, Set<Runnable>> hooks;
        synchronized (SHUTDOWN_HOOKS) {
          hooks = new TreeMap<>(SHUTDOWN_HOOKS);
          for (Map.Entry<Integer, Set<Runnable>> entry : hooks.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
          }
        }
        for (Map.Entry<Integer, Set<Runnable>> runnables : hooks.entrySet()) {
          final Set<Runnable> values = runnables.getValue();
          if (values.size() <= 1) {
            for (Runnable runnable : values) {
              try {
                runnable.run();
              } catch (RuntimeException ex) {
                if (rex == null) {
                  rex = ex;
                } else {
                  rex.addSuppressed(ex);
                }
              }
            }
          } else if (((int) runnables.getKey()) >= Integer.MAX_VALUE) {
            Thread[] threads = new Thread[values.size()];
            int i = 0;
            for (Runnable runnable : values) {
              Thread thread = new Thread(runnable);
              thread.start();
              threads[i++] = thread;
            }
            long deadline = System.currentTimeMillis() + WAIT_FOR_SHUTDOWN_MILLIS;
            for (Thread thread : threads) {
              try {
                thread.join(deadline - System.currentTimeMillis());
              } catch (InterruptedException ex) {
                if (rex == null) {
                  rex = ex;
                } else {
                  rex.addSuppressed(ex);
                }
              }
            }
          } else {
            List<Future<?>> futures = new ArrayList<>(values.size());
            for (Runnable runnable : values) {
              futures.add(DefaultExecutor.INSTANCE.submit(runnable));
            }
            for (Future<?> future : futures) {
              try {
                future.get();
              } catch (InterruptedException | ExecutionException | RuntimeException ex) {
                if (rex == null) {
                  rex = ex;
                } else {
                  rex.addSuppressed(ex);
                }
              }
            }
          }
        }
        // print out info on all remaining non daemon threads.
        Thread[] threads = FastStackCollector.getThreads();
        Thread current = Thread.currentThread();
        boolean first = true;
        for (Thread thread : threads) {
          if (thread.isAlive() && !thread.isDaemon() && !thread.equals(current)
                  && !thread.getName().contains("DestroyJavaVM")) {
            if (first) {
              System.err.println("Non daemon threads still running:");
              first = false;
            }
            System.err.println("Non daemon thread " + thread + ", stackTrace = "
                    + java.util.Arrays.toString(thread.getStackTrace()));
          }
        }
        if (rex != null) {
          throw rex;
        }
      }
    }, "spf4j queued shutdown"));
    JAVA_PLATFORM = Version.fromSpecVersion(JAVA_VERSION);
    Registry.export(Jmx.class);
  }

  public enum Version {

    V1_0, V1_1, V1_2, V1_3, V1_4, V1_5, V1_6, V1_7, V1_8, V1_9_PLUSZ;

    public static Version fromSpecVersion(final String specVersion) {
      return Version.values()[Integer.parseInt(specVersion.split("\\.")[1])];
    }
  }

  private static class Lazy {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lazy.class);
  }

  private Runtime() {
  }

  public static void goDownWithError(final SysExits exitCode) {
    goDownWithError(null, exitCode.exitCode());
  }

  public static void goDownWithError(@Nullable final Throwable t, final SysExits exitCode) {
    goDownWithError(t, exitCode.exitCode());
  }

  // Calling Halt is the only sensible thing to do when the JVM is hosed.
  @SuppressFBWarnings("MDM_RUNTIME_EXIT_OR_HALT")
  public static void goDownWithError(@Nullable final Throwable t, final int exitCode) {
    try {
      if (t != null) {
        Throwables.writeTo(t, System.err, Throwables.PackageDetail.NONE); //High probability attempt to log first
        Throwables.writeTo(t, System.err, Throwables.PackageDetail.SHORT); //getting more curageous :-)
        Lazy.LOGGER.error("Error, going down with exit code {}", exitCode, t); //Now we are pushing it...
      } else {
        Lazy.LOGGER.error("Error, going down with exit code {}", exitCode);
      }
    } finally {
      java.lang.Runtime.getRuntime().halt(exitCode);
    }
  }

  public static boolean isMacOsx() {
    return IS_MAC_OSX;
  }

  public static boolean isWindows() {
    return IS_WINDOWS;
  }

  public static boolean haveJnaPlatform() {
    URL resource = Thread.currentThread().getContextClassLoader().getResource("com/sun/jna/platform/package.html");
    return (resource != null);
  }

  @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_RETURN_FALSE")
  public static boolean haveJnaPlatformClib() {
    try {
      Class.forName("com.sun.jna.platform.unix.LibC");
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * get the number of open files by current java process.
   *
   * @return -1 if cannot get nr of open files
   */
  @CheckReturnValue
  @Deprecated
  public static int getNrOpenFiles() {
    return (int) OperatingSystem.getOpenFileDescriptorCount();
  }

  /**
   * @deprecated use Lsof.getLsofOutput instead.
   */
  @Nullable
  @CheckReturnValue
  @Deprecated
  public static CharSequence getLsofOutput() {
    return Lsof.getLsofOutput();
  }

  /**
   * @deprecated use Processhandler
   */
  @Deprecated
  public interface ProcOutputHandler {

    void handleStdOut(byte[] bytes, int length);

    void stdOutDone();

    void handleStdErr(byte[] bytes, int length);

    void stdErrDone();
  }

  /**
   * @deprecated use OperatingSystem.forkExec.
   */
  @Deprecated
  public static CharSequence run(final String[] command,
          final long timeoutMillis) throws IOException, InterruptedException, ExecutionException, TimeoutException {
    return OperatingSystem.forkExec(command, timeoutMillis);
  }

  /**
   * @deprecated use OperatingSystem.killProcess.
   */
  @Deprecated
  public static int killProcess(final Process proc, final long terminateTimeoutMillis,
          final long forceTerminateTimeoutMillis)
          throws InterruptedException {
    return OperatingSystem.killProcess(proc, terminateTimeoutMillis, forceTerminateTimeoutMillis);
  }

  /**
   * @deprecated use OperatingSystem.forkExec instead.
   */
  public static int run(final String[] command, final ProcOutputHandler handler,
          final long timeoutMillis)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    return run(command, handler, timeoutMillis, 60000);
  }

  /**
   * @deprecated use OperatingSystem.forkExec instead.
   */
  @SuppressFBWarnings("COMMAND_INJECTION")
  @Deprecated
  public static int run(final String[] command, final ProcOutputHandler handler,
          final long timeoutMillis, final long terminationTimeoutMillis)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    ProcessResponse<Void, Void> resp = OperatingSystem.forkExec(command,
            new ProcessHandler<Void, Void>() {
      @Override
      public Void handleStdOut(final InputStream is) throws IOException {
        int cos;
        byte[] buffer = ArraySuppliers.Bytes.TL_SUPPLIER.get(8192);
        try {
          while ((cos = is.read(buffer)) >= 0) {
            handler.handleStdOut(buffer, cos);
          }
        } finally {
          ArraySuppliers.Bytes.TL_SUPPLIER.recycle(buffer);
          handler.stdOutDone();
        }
        return null;
      }

      @Override
      public Void handleStdErr(final InputStream stderr) throws IOException {
        int cos;
        byte[] buffer = ArraySuppliers.Bytes.TL_SUPPLIER.get(8192);
        try {
          while ((cos = stderr.read(buffer)) >= 0) {
            handler.handleStdErr(buffer, cos);
          }
        } finally {
          ArraySuppliers.Bytes.TL_SUPPLIER.recycle(buffer);
          handler.stdErrDone();
        }
        return null;
      }
    }, timeoutMillis, terminationTimeoutMillis);
    return resp.getResponseCode();
  }

  public static void queueHookAtBeginning(final Runnable runnable) {
    synchronized (SHUTDOWN_HOOKS) {
      queueHook(Integer.MIN_VALUE, runnable);
    }
  }

  public static void queueHookAtEnd(final Runnable runnable) {
    queueHook(Integer.MAX_VALUE, runnable);
  }

  public static void queueHook(final int priority, final Runnable runnable) {
    synchronized (SHUTDOWN_HOOKS) {
      Integer pr = priority;
      Set<Runnable> runnables = SHUTDOWN_HOOKS.get(pr);
      if (runnables == null) {
        runnables = new HashSet<>();
        SHUTDOWN_HOOKS.put(pr, runnables);
      }
      runnables.add(runnable);
    }
  }

  public static boolean removeQueuedShutdownHook(final Runnable runnable) {
    if ("spf4j queued shutdown".equals(Thread.currentThread().getName())) {
      return false;
    }
    synchronized (SHUTDOWN_HOOKS) {
      for (Set<Runnable> entry : SHUTDOWN_HOOKS.values()) {
        if (entry.remove(runnable)) {
          return true;
        }
      }
    }
    return false;
  }

  public static long getDeadline() {
    return DEADLINE.get();
  }

  public static long millisToDeadline() throws TimeoutException {
    final long deadline = DEADLINE.get();
    long result = deadline - System.currentTimeMillis();
    if (result < 0) {
      throw new TimeoutException("Deadline passed " + ISODateTimeFormat.basicDateTime().print(deadline));
    } else {
      return result;
    }
  }

  public static void setDeadline(final long deadline) {
    DEADLINE.set(deadline);
  }

  /**
   * Attempts to run the GC in a verifiable way.
   *
   * @param timeoutMillis - timeout for GC attempt
   * @return true if GC executed for sure, false otherwise, gc might have been executed though, but we cannot be sure.
   */
  @SuppressFBWarnings
  public static boolean gc(final long timeoutMillis) {
    WeakReference<Object> ref = new WeakReference<>(new Object());
    long deadline = System.currentTimeMillis() + timeoutMillis;
    do {
      System.gc();
    } while (ref.get() != null && System.currentTimeMillis() < deadline);
    return ref.get() == null;
  }

  public static CharSequence jrun(final Class<?> classWithMain,
          final long timeoutMillis, final String... arguments)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
    return jrun(classWithMain, classPath, timeoutMillis, arguments);
  }

  public static CharSequence jrun(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    JVMArguments inputArguments = new JVMArguments(ManagementFactory.getRuntimeMXBean().getInputArguments());
    inputArguments.removeAllSystemPropertiesStartingWith("com.sun.management.jmxremote");
    return jrun(classWithMain, classPath, timeoutMillis, inputArguments.toArray(), arguments);
  }

  public static CharSequence jrun(final Class<?> classWithMain, final String classPath, final long timeoutMillis,
          final String[] jvmArgs,
          final String... arguments) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final String jvmPath = JAVA_HOME + File.separatorChar + "bin" + File.separatorChar + "java";
    String[] command = Arrays.concat(new String[]{jvmPath},
            jvmArgs,
            new String[]{"-cp", classPath, classWithMain.getName()},
            arguments);
    return OperatingSystem.forkExec(command, timeoutMillis);
  }

  /**
   * get the main Thread.
   * @return null if there is no main thread (can happen when calling this is a shutdown hook)
   */
  @Nullable
  public static Thread getMainThread() {
    Thread[] threads = FastStackCollector.getThreads();
    for (Thread t : threads) {
      if (t.getId() == 1L) {
        return t;
      }
    }
    return null;
  }

  /**
   * @return null if main class cannot be found.
   */
  @Nullable
  public static Class<?> getMainClass() {
    Thread mainThread = getMainThread();
    if (mainThread == null) {
      return null;
    }
    StackTraceElement[] stackTrace = mainThread.getStackTrace();
    if (stackTrace.length == 0) {
      return null;
    }
    String className = stackTrace[stackTrace.length - 1].getClassName();
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      NoClassDefFoundError tex = new NoClassDefFoundError("Cannot find " + className);
      tex.initCause(ex);
      throw tex;
    }
  }


  public static final class Jmx {

    @JmxExport
    public static Reflections.PackageInfo getPackageInfo(@JmxExport("className") final String className) {
      return Reflections.getPackageInfo(className);
    }

    @JmxExport
    public static void restart() throws IOException {
      UnixRuntime.restart();
    }

  }

}
