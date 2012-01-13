package de.fiz.escidoc.factory.cli;

public abstract class ProgressBar {
	public static void printProgressBar(int percent) {
		printProgressBar(percent, false);
	}

	public static void printProgressBar(int percent, boolean lineBreak) {
		StringBuilder bar = new StringBuilder("[");
		for (int i = 0; i < 50; i++) {
			if (i < (percent / 2)) {
				bar.append("=");
			} else if (i == (percent / 2)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}

		bar.append("]   " + percent + "%     ");
		System.out.print("\r" + bar.toString());
		if (lineBreak) {
			System.out.println();
		}
	}
}
