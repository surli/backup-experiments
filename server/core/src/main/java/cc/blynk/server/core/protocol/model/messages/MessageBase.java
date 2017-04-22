package cc.blynk.server.core.protocol.model.messages;

import cc.blynk.server.core.protocol.enums.Command;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 * Yes, I don't use getters and setters, inlining is not always works as expected.
 *
 * IMPORTANT : have in mind, in body we retrieve always unsigned bytes, shorts, while in java
 * is only signed types, so we require 2 times larger types.
 */
public abstract class MessageBase {

    //1 + 2 + 2
    public static final int HEADER_LENGTH = 5;

    public final short command;

    public final int id;

    public final int length;

    public MessageBase(int id, short command, int length) {
        this.command = command;
        this.id = id;
        this.length = length;
    }

    public abstract byte[] getBytes();

    @Override
    public String toString() {
        return "id=" + id +
                ", command=" + Command.getNameByValue(command) +
                ", length=" + length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageBase that = (MessageBase) o;

        if (command != that.command) return false;
        if (id != that.id) return false;
        if (length != that.length) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) command;
        result = 31 * result + id;
        result = 31 * result + length;
        return result;
    }
}
