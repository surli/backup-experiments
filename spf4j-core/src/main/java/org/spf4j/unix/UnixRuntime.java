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
package org.spf4j.unix;

import com.sun.jna.Native;
import java.io.IOException;
import static org.spf4j.base.Runtime.haveJnaPlatformClib;

/**
 * @author Zoltan Farkas
 */
public final class UnixRuntime {

  private UnixRuntime() {
  }

  public static void restart() throws IOException {
    if (haveJnaPlatformClib()) {
      JVMArguments current = JVMArguments.current();
      String existing = current.removeSystemProperty("spf4j.restart");
      int count;
      if (existing == null) {
        count = 1;
      } else {
        count = Integer.parseInt(existing) + 1;
      }
      current.setSystemProperty("spf4j.restart", Integer.toString(count));
      // close all files upon exec, except stdin, stdout, and stderr
      int sz = CLibrary.INSTANCE.getdtablesize();
      for (int i = 3; i < sz; i++) {
        int flags = CLibrary.INSTANCE.fcntl(i, CLibrary.F_GETFD);
        if (flags < 0) {
          continue;
        }
        CLibrary.INSTANCE.fcntl(i, CLibrary.F_SETFD, flags | CLibrary.FD_CLOEXEC);
      }

      // exec to self
      String exe = current.getExecutable();
      CLibrary.INSTANCE.execvp(exe, current.toStringArray());
      throw new IOException("Failed to exec '" + exe + "' " + CLibrary.INSTANCE.strerror(Native.getLastError()));

    } else {
      throw new UnsupportedOperationException();
    }
  }

}
