//
//  Keychain.h
//  store
//
//  Created by Sungoh Kang on 2021/06/25.
//

#import <Foundation/Foundation.h>
typedef void (^KeychainOperationBlock)(BOOL successfulOperation, NSData *data, OSStatus status);

@interface Keychain : NSObject

- (id)initWithService:(NSString *)service withGroup:(NSString *)group;

- (void)insertKey:(NSString *)key withData:(NSData *)data withCompletion:(KeychainOperationBlock)completionBlock;
- (void)updateKey:(NSString *)key withData:(NSData *)data withCompletion:(KeychainOperationBlock)completionBlock;
- (void)removeDataForKey:(NSString *)key withCompletionBlock:(KeychainOperationBlock)completionBlock;
- (void)findDataForKey:(NSString *)key withCompletionBlock:(KeychainOperationBlock)completionBlock;

@end
