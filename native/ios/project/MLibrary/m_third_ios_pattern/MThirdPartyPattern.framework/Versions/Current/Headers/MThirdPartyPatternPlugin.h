//
//  MThirdPartyPatternPlugin.h
//

#import <Foundation/Foundation.h>

@interface MThirdPartyPatternPlugin : MPlugin

+ (MThirdPartyPatternPlugin *)getInstance;

@end

#define PLUGIN_CLASS    MThirdPartyPatternPlugin
#define PLUGIN_BUNDLE   @"MThirdPartyPattern.bundle"

#define PGResource(res) [PLUGIN_BUNDLE appendPath:res]
#define PGLocalizedString(key, comment) [PLUGIN_CLASS localizedStringForKey:(key)]
