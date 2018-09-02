package uk.org.toot.control.id;

/**
 * ProviderId defines known provider IDs and should be used to ensure control
 * services from different providers have their own id-spaces which are portable
 * between installations on different machines.
 * Unknown provider IDs will function but cannot be guaranteed to be portable.
 */
public interface ProviderId 
{
    // these constant values MUST NEVER CHANGE, only add new constants
    // to ensure persistent id use remains valid
    // best practice is to import static the single constant you need
	// 0 is CompoundControl.USE_PARENT_PROVIDER_ID, the default id
    static final int TOOT_PROVIDER_ID = 1;
    static final int FRINIKA_PROVIDER_ID = 2;
    
    // to allow 127 differents VST effects
    static final int VST_PROVIDER_ID = 126;
    
    // this constant can be used to develop unreleased services.
    // TootSoftware will be pleased to add a proper provider ID for you to
    // release services. See toot.org.uk and join the discussion group to
    // request a proper provider ID. 
    static final int TEMPORARY_PROVIDER_ID = 127;
}
