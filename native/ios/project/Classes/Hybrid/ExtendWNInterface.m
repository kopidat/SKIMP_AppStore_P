//
//  ExtendWNInterface.m
//

#import "ExtendWNInterface.h"
#import "Keychain.h"
#import "NSData+AES.h"

#define AES256_KEY @"com.skimp.commoncom.skimp.common"
#define KEYCHAIN_USERINFO @"USER_INFO"

@interface ExtendWNInterface ()

@end

@implementation ExtendWNInterface

- (id)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (BOOL)checkValidParameters:(NSDictionary *)parameters fromValidClasses:(NSDictionary *)validClasses errorMessage:(NSString **)errorMessage {
    
    for ( NSString *key in validClasses ) {
        Class validClass = [validClasses objectForKey:key];
        id parameter = [parameters objectForKey:key];
        
        if ( parameter == nil ) {
            *errorMessage = [NSString stringWithFormat:@"key(%@) is null", key];
            return NO;
        }
        
        if ( ![parameter isKindOfClass:validClass] ) {
            *errorMessage = [NSString stringWithFormat:@"key(%@) is invalid type", key];
            return NO;
        }
        
        if ( [validClass isEqual:[NSString class]] && [[parameter stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] isEqualToString:@""]) {
            *errorMessage = [NSString stringWithFormat:@"key(%@) is invalid", key];
            return NO;
        }
        else if ( [validClass isEqual:[NSDictionary class]] ) {
            *errorMessage = [NSString stringWithFormat:@"key(%@) is invalid", key];
            return NO;
        }
        else if ( [validClass isKindOfClass:[NSArray class]] ) {
            *errorMessage = [NSString stringWithFormat:@"key(%@) is invalid", key];
            return NO;
        }
    }
    
    return YES;
}

- (BOOL)isAlive {
    if (![[PPNavigationController ppNavigationController] existViewController:_viewController]) {
        PPDebug(@"Already released view controller!!");
        return NO;
    }
    
    return YES;
}

// Callback 구조를 설명하기 위한 Sample Interface
- (NSString *)wnSampleCallback:(NSString *)jsonString {
    NSDictionary *options = [jsonString objectFromJsonString];
    
    if (!options) {
        return [@{@"status":@"FAIL", @"error":@"invalid params"} jsonString];
    }

    NSString *invalidMessage = nil;
    NSDictionary *validClasses = @{
        @"callback": [NSString class]
    };
    
    if ( ! [self checkValidParameters:options fromValidClasses:validClasses errorMessage:&invalidMessage] ) {
        return [@{@"status":@"FAIL", @"error":invalidMessage} jsonString];
    }
    
    NSString *callback = [options objectForKey:@"callback"];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.35 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        NSDictionary *resultInfo = @{
            @"status": @"SUCCESS"
        };
        
        [self.viewController callCbfunction:callback withObjects:resultInfo, nil];
    });
    
    return [@{ @"status": @"PROCESSING" } jsonString];
}

- (void)exWNSetKeychainData:(NSString *)value :(NSString *)key {
    NSLog(@"value : %@", value);
    NSLog(@"key : %@", key);
    
    NSData *data = [value dataUsingEncoding:NSUTF8StringEncoding];
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain insertKey:@"UESR_INFO" withData:data withCompletion:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
            NSLog(@"status : %d", (int)status);
        }
    }];
}

- (void)exWNGetKeychainData:(NSString *)key {
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain findDataForKey:key withCompletionBlock:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
            NSLog(@"status : %d", (int)status);
        }
    }];
}

- (void)exWNSetUserInfo:(NSString *)userInfo {
    NSLog(@"userInfo : %@", userInfo);
    
    NSDictionary *userInfoDic = [userInfo objectFromJsonString];
    
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:userInfoDic];
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain insertKey:[NSString stringWithFormat:@"%@", KEYCHAIN_USERINFO] withData:data withCompletion:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        NSDictionary *resultDic = (NSDictionary *)[NSKeyedUnarchiver unarchiveObjectWithData:data];
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        }
    }];
}

- (void)exWNGetUserInfo {
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain findDataForKey:[NSString stringWithFormat:@"%@", KEYCHAIN_USERINFO]  withCompletionBlock:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        NSDictionary *resultDic = (NSDictionary *)[NSKeyedUnarchiver unarchiveObjectWithData:data];
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        }
    }];
}

- (NSString *)exWNAES256Encrypt:(NSString *)plainStr {
    NSData *plainStrData = [plainStr dataUsingEncoding:NSUTF8StringEncoding];
    NSData *encData = [plainStrData AES256Encrypt:AES256_KEY];
    
    NSString *encStr = [encData base64EncodedStringWithOptions:0];
    
    return encStr;
}

- (NSString *)exWNAES256Decrypt:(NSString *)encStr {
    NSData *encStrData = [[NSData alloc] initWithBase64EncodedString:encStr options:0];
    NSData *decData = [encStrData AES256Decrypt:AES256_KEY];
    
    NSString *decStr = [[NSString alloc] initWithData:decData encoding:NSUTF8StringEncoding];
    
    return decStr;
}


@end
