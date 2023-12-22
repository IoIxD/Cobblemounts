package net.ioixd;


public interface MountIsMoving {
    default boolean mount_isMoving(){return false;};
    default void mount_setMoving(boolean b){};
}
