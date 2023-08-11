package ru.kelcuprum.waterplayer.output;

@FunctionalInterface
public interface IOutputConsumer {
	
	void accept(byte[] buffer, int chunkSize);
	
}
