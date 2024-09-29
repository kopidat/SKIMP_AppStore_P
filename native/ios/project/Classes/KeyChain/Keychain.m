//
//  Keychain.m
//  store
//
//  Created by Sungoh Kang on 2021/06/25.
//

#import "Keychain.h"
#import <Security/Security.h>

@implementation Keychain
{
    NSString *keychainService;
    NSString *keychainGroup;
}

- (id)initWithService:(NSString *)service withGroup:(NSString *)group {
    self = [super init];
    if (self) {
        keychainService = [NSString stringWithString:service];
        if (group) {
            keychainGroup = [NSString stringWithString:group];
        }
    }
    
    return self;
}

- (void)insertKey:(NSString *)key
         withData:(NSData *)data
   withCompletion:(KeychainOperationBlock)completionBlock
{
    NSMutableDictionary *dict = [self prepareDict:key];
    [dict setObject:data forKey:(__bridge id)kSecValueData];
    [dict setObject:keychainService forKey:(id)kSecAttrService];
    
    OSStatus status = SecItemAdd((__bridge CFDictionaryRef)dict, NULL);
    if (errSecSuccess != status) {
        NSLog(@"Unable add item with key = %@ error : %d", key, (int)status);
        if (completionBlock) {
            completionBlock(errSecSuccess == status, nil, status);
        }
    }
    if (status == errSecDuplicateItem) {
        [self updateKey:key withData:data withCompletion:^(BOOL successfulOperation, NSData *updateData, OSStatus updateStatus) {
            if (completionBlock) {
                completionBlock(successfulOperation, updateData, updateStatus);
            }
            NSLog(@"Found duplication item -- updating key with data");
        }];
    }
}

- (void)findDataForKey:(NSString *)key
   withCompletionBlock:(KeychainOperationBlock)completionBlock
{
    NSMutableDictionary *dict = [self prepareDict:key];
    [dict setObject:(__bridge id)kSecMatchLimitOne forKey:(__bridge id)kSecMatchLimit];
    [dict setObject:keychainService forKey:(id)kSecAttrService];
    [dict setObject:(id)kCFBooleanTrue forKey:(__bridge id)kSecReturnData];
    CFTypeRef result = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef)dict, &result);
    
    if (status != errSecSuccess) {
        NSLog(@"Unable to fetch item for key %@ with error : %d", key, (int)status);
        if (completionBlock) {
            completionBlock(errSecSuccess == status, nil, status);
        }
    } else {
        if (completionBlock) {
            completionBlock(errSecSuccess == status, (__bridge NSData *)result, status);
        }
    }
}

- (void)updateKey:(NSString *)key
         withData:(NSData *)data
   withCompletion:(KeychainOperationBlock)completionBlock
{
    NSMutableDictionary *dictKey = [self prepareDict:key];
    
    NSMutableDictionary *dictUpdate = [[NSMutableDictionary alloc] init];
    [dictUpdate setObject:data forKey:(__bridge id)kSecValueData];
    [dictUpdate setObject:keychainService forKey:(id)kSecAttrService];
    OSStatus status = SecItemUpdate((__bridge CFDictionaryRef)dictKey, (__bridge CFDictionaryRef)dictUpdate);
    if (status != errSecSuccess) {
        NSLog(@"Unable to remove item for key %@ with error : %d", key, (int)status);
    }
    if (completionBlock) {
        completionBlock(errSecSuccess == status, nil, status);
    }
}

- (void)removeDataForKey:(NSString *)key
     withCompletionBlock:(KeychainOperationBlock)completionBlock
{
    NSMutableDictionary *dict = [self prepareDict:key];
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef)dict);
    if (status != errSecSuccess) {
        NSLog(@"Unable to remove item for key %@ with error : %d", key, (int)status);
    }
    if (completionBlock) {
        completionBlock(errSecSuccess == status, nil, status);
    }
}

#pragma mark Internal methods

- (NSMutableDictionary *)prepareDict:(NSString *)key {
    
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
    [dict setObject:(__bridge id)kSecClassGenericPassword forKey:(__bridge id)kSecClass];
    
    NSData *encodedKey = [key dataUsingEncoding:NSUTF8StringEncoding];
    [dict setObject:encodedKey forKey:(__bridge id)kSecAttrGeneric];
    [dict setObject:encodedKey forKey:(__bridge id)kSecAttrAccount];
    [dict setObject:keychainService forKey:(__bridge id)kSecAttrService];
    [dict setObject:(__bridge id)kSecAttrAccessibleAlwaysThisDeviceOnly forKey:(__bridge id)kSecAttrAccessible];
    
    // This is for sharing data across apps
    if (keychainGroup != nil) {
        [dict setObject:keychainGroup forKey:(__bridge id)kSecAttrAccessGroup];
    }
    
    return dict;
}

@end
