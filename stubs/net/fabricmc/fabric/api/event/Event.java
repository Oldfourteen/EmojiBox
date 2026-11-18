package net.fabricmc.fabric.api.event;

/** 编译期桩，运行时由 Fabric API 提供真实实现，不打包进 mod。 */
public abstract class Event<T> {
    public abstract void register(T listener);
}
