//
//  NSData+AES.m
//  SKIMPAppStoreCommon
//
//  Created by Sungoh Kang on 2021/06/28.
//

#import "NSData+AES.h"
#import <CommonCrypto/CommonCryptor.h>

@implementation NSData (AES)

- (NSData *)AES256Encrypt:(NSString *)key {
    return [self iAESEncrypt:key keySize:kCCKeySizeAES256];
}

- (NSData *)AES128Encrypt:(NSString *)key {
    return [self iAESEncrypt:key keySize:kCCKeySizeAES128];
}

- (NSData *)iAESEncrypt:(NSString *)key keySize:(int)keySize {
    if (key == nil) {
        return nil;
    }
    
    char keyPtr[keySize + 1];
    bzero(keyPtr, sizeof(keyPtr));
    
    [key getCString:keyPtr maxLength:sizeof(keyPtr) encoding:NSUTF8StringEncoding];
    size_t numBytesEncrypted = 0x00;
    
    NSUInteger dataLength = [self length];
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer = malloc(bufferSize);
    const unsigned char iv[] = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15,0x16};

    CCCryptorStatus result = CCCrypt(kCCEncrypt,
                                     kCCAlgorithmAES128,
                                     kCCOptionPKCS7Padding,
                                     keyPtr,
                                     keySize,
                                     iv,
                                     [self bytes], [self length],
                                     buffer, bufferSize,
                                     &numBytesEncrypted);
    
    if (result == kCCSuccess) {
        return [NSData dataWithBytesNoCopy:buffer length:numBytesEncrypted];
    } else {
        NSLog(@"iAESEncrypt FAIL!");
    }
    
    free(buffer);
    return nil;
}

- (NSData *)AES256Decrypt:(NSString *)key {
    return [self iAESDecrypt:key keySize:kCCKeySizeAES256];
}

- (NSData *)AES128Decrypt:(NSString *)key {
    return [self iAESDecrypt:key keySize:kCCKeySizeAES128];
}

- (NSData *)iAESDecrypt:(NSString *)key keySize:(int)keySize {
    if (key == nil) {
        return nil;
    }
    
    char keyPtr[keySize + 0x01];
    bzero(keyPtr, sizeof(keyPtr));
    
    // fetch key data
    [key getCString:keyPtr maxLength:sizeof(keyPtr) encoding:NSUTF8StringEncoding];
    
    NSUInteger dataLength = [self length];
    size_t bufferSize = dataLength + kCCBlockSizeAES128;
    void *buffer_decrypt = malloc(bufferSize);
    const unsigned char iv[] = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15,0x16};
    
    size_t numBytesDecrypted = 0x00;
    CCCryptorStatus result = CCCrypt(kCCDecrypt,
                                     kCCAlgorithmAES128,
                                     kCCOptionPKCS7Padding,
                                     keyPtr,
                                     keySize,
                                     iv,
                                     [self bytes], [self length],
                                     buffer_decrypt, bufferSize,
                                     &numBytesDecrypted);
    
    if (result == kCCSuccess) {
        return [NSData dataWithBytesNoCopy:buffer_decrypt length:numBytesDecrypted];
    } else {
        NSLog(@"iAESDecrypt FAIL!");
    }
    
    return nil;
}

@end
