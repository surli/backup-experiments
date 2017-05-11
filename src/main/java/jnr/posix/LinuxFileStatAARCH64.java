package jnr.posix;

import jnr.ffi.StructLayout;
import jnr.posix.util.Platform;

public final class LinuxFileStatAARCH64 extends BaseFileStat implements NanosecondFileStat {
    public static final class Layout extends StructLayout {

        public Layout(jnr.ffi.Runtime runtime) {
            super(runtime);
        }

        public final Signed64 st_dev = new Signed64();
        public final Unsigned64 st_ino = new Unsigned64();
        public final Unsigned32 st_mode = new Unsigned32();
        public final Signed32 st_nlink = new Signed32();
        public final Unsigned32 st_uid = new Unsigned32();
        public final Unsigned32 st_gid = new Unsigned32();
        public final Signed64 st_rdev = new Signed64();
        public final Signed64 __pad1 = new Signed64();
        public final Signed64 st_size = new Signed64();
        public final Signed32 st_blksize = new Signed32();
        public final Signed32 __pad2 = new Signed32();
        public final Signed64 st_blocks = new Signed64();
        public final time_t st_atime = new time_t();             // Time of last access
        public final SignedLong st_atimensec = new SignedLong(); // Time of last access (nanoseconds)
        public final time_t st_mtime = new time_t();             // Last data modification time
        public final SignedLong st_mtimensec = new SignedLong(); // Last data modification time (nanoseconds)
        public final time_t st_ctime = new time_t();             // Time of last status change
        public final SignedLong st_ctimensec = new SignedLong(); // Time of last status change (nanoseconds)
        public final Signed64 __unused4 = new Signed64();
    }

    private static final Layout layout = new Layout(jnr.ffi.Runtime.getSystemRuntime());

    public LinuxFileStatAARCH64(LinuxPOSIX posix) {
        super(posix, layout);
    }

    public long atime() {
        return layout.st_atime.get(memory);
    }

    public long aTimeNanoSecs() {
        return layout.st_atimensec.get(memory);
    }

    public long blockSize() {
        return layout.st_blksize.get(memory);
    }

    public long blocks() {
        return layout.st_blocks.get(memory);
    }

    public long ctime() {
        return layout.st_ctime.get(memory);
    }

    public long cTimeNanoSecs() {
        return layout.st_ctimensec.get(memory);
    }

    public long dev() {
        return layout.st_dev.get(memory);
    }

    public int gid() {
        return (int) layout.st_gid.get(memory);
    }

    public long ino() {
        return layout.st_ino.get(memory);
    }

    public int mode() {
        return (int) layout.st_mode.get(memory);
    }

    public long mtime() {
        return layout.st_mtime.get(memory);
    }

    public long mTimeNanoSecs() {
        return layout.st_mtimensec.get(memory);
    }

    public int nlink() {
        return (int) layout.st_nlink.get(memory);
    }

    public long rdev() {
        return layout.st_rdev.get(memory);
    }

    public long st_size() {
        return layout. st_size.get(memory);
    }

    public int uid() {
        return (int) layout.st_uid.get(memory);
    }
}
