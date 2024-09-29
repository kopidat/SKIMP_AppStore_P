//
//  NSData+AES.h
//  SKIMPAppStoreCommon
//
//  Created by Sungoh Kang on 2021/06/28.
//

#import <Foundation/Foundation.h>

@interface NSData (AES)

- (NSData *)AES128Encrypt:(NSString *)key;
- (NSData *)AES128Decrypt:(NSString *)key;

- (NSData *)AES256Encrypt:(NSString *)key;
- (NSData *)AES256Decrypt:(NSString *)key;

@end
