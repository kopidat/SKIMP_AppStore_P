//
//  MThirdPartyFingerprintBasicPlugin.h
//

#import <Foundation/Foundation.h>

@interface MThirdPartyFingerprintBasicPlugin : MPlugin

+ (MThirdPartyFingerprintBasicPlugin *)getInstance;

@end

#define PLUGIN_CLASS    MThirdPartyFingerprintBasicPlugin
#define PLUGIN_BUNDLE   @"MThirdPartyFingerprintBasic.bundle"

#define PGResource(res) [PLUGIN_BUNDLE appendPath:res]
#define PGLocalizedString(key, comment) [PLUGIN_CLASS localizedStringForKey:(key)]
