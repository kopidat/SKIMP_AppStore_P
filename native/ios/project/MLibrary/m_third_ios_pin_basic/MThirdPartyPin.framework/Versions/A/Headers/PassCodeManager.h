//
//  PassCodeManager.h
//  BAROTALK
//
//  Created by ProgDesigner on 2016. 7. 1..
//
//

#import <Foundation/Foundation.h>

typedef void (^PassCodeManagerCompletionHandler) (NSString *status);

@interface PassCodeManager : NSObject

@property (nonatomic, strong) NSDate *lastUseDate;
@property (nonatomic, readonly) NSString *userPassCode;
@property (nonatomic, copy) PassCodeManagerCompletionHandler completionHandler;

- (BOOL)hasPassCode;

- (BOOL)needsToAuth;

- (BOOL)registerPassCode:(NSString *)passCode;

- (BOOL)validPassCode:(NSString *)passCode;

- (NSString *)userPassCode;
- (void)clearData;

- (void)showMode:(NSString *)name mode:(NSString *)mode length:(NSString *)maxlen completion:(PassCodeManagerCompletionHandler)completionHandler;

+ (PassCodeManager *)defaultManager;

@end
