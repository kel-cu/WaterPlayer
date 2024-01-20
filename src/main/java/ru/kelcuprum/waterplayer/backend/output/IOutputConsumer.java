package ru.kelcuprum.waterplayer.backend.output;

@FunctionalInterface
public interface IOutputConsumer {
	
	void accept(byte[] buffer, int chunkSize);
	
}
