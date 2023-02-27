package me.glindholm.connector.eclipse.internal.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import me.glindholm.connector.eclipse.internal.bamboo.core.client.BambooClient;

public interface IBambooClientFactory {
    BambooClient getBambooClient(TaskRepository repository);
}
