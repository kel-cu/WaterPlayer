package ru.kelcuprum.waterplayer.api.output;

@FunctionalInterface
public interface IOutputConsumer {
	
	void accept(byte[] buffer, int chunkSize);
	
}
