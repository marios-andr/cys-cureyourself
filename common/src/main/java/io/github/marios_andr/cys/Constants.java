package io.github.marios_andr.cys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "cys";
	public static final String MOD_NAME = "Cure Your Self";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static boolean isEnabled;
	public static boolean isPeacefulEnabled;
	public static boolean isHardcoreCuringEnabled;
	public static boolean zombieImmunity;
	public static ExperienceOptions zombieExperienceOptions;

	public enum ExperienceOptions {
		NONE,
		STORE_PARTIAL,
		STORE_FULL
	}
}