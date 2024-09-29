//
//  NotificationService.m
//  NotificationService
//
//  Created by Sungoh Kang on 2021/06/08.
//

#import "NotificationService.h"
#import <MPush/PushManager.h>
#import <MPush/PushManagerInfo.h>
#import "Keychain.h"

@interface NotificationService ()

@property (nonatomic, strong) void (^contentHandler)(UNNotificationContent *contentToDeliver);
@property (nonatomic, strong) UNMutableNotificationContent *bestAttemptContent;

@end

@implementation NotificationService

- (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler {
    self.contentHandler = contentHandler;
    self.bestAttemptContent = [request.content mutableCopy];
    
    // Modify the notification content here...
//    self.bestAttemptContent.title = [NSString stringWithFormat:@"%@ [modified]", self.bestAttemptContent.title];
    
    // PushManager 초기화
    PushManager *manager = [PushManager defaultManager];
    
    NSDictionary *userInfoDic = [[NSDictionary alloc] init];
    NSDictionary *mpsDic = [[NSDictionary alloc] init];
    
    userInfoDic = self.bestAttemptContent.userInfo;
    mpsDic = [userInfoDic objectForKey:@"mps"];
    
    dispatch_group_t group = dispatch_group_create();
    dispatch_group_enter(group);
    NSDictionary __block *resultDic = [[NSDictionary alloc] init];
    
    NSString *bundleSeedId = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"AppIdentifierPrefix"];
    Keychain *keychain = [[Keychain alloc] initWithService:@"SKIMPAppStoreService" withGroup:[NSString stringWithFormat:@"%@%@", bundleSeedId, @"SKIPartnerStoreInfo"]];
    [keychain findDataForKey:@"pushUserInfo"  withCompletionBlock:^(BOOL successfulOperation, NSData *data, OSStatus status) {
        resultDic = (NSDictionary *)[NSKeyedUnarchiver unarchiveObjectWithData:data];
        if (successfulOperation) {
            NSLog(@"successfulOperation true");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        } else {
            NSLog(@"successfulOperation false");
            NSLog(@"result Data : %@", resultDic);
            NSLog(@"status : %d", (int)status);
        }
        dispatch_group_leave(group);
    }];
    
    dispatch_group_wait(group, DISPATCH_TIME_FOREVER);
    
    if (resultDic == nil) {
        self.contentHandler(self.bestAttemptContent);
        return;
    } else {
        NSString *cuid = [resultDic objectForKey:@"cuid"];
        NSString *psid = [resultDic objectForKey:@"psid"];
        NSString *host = [resultDic objectForKey:@"host"];
        
        if (host) {
            [manager.info changeHost:host];
        }
        if (cuid && psid) {
            // feedback API 호출
            // push메시지 객체와 cuid, psid값이 필요
            [manager feedback:self notification:self.bestAttemptContent.userInfo clientUID:cuid psID:psid completionHandler:^(BOOL success) {
                // 앱에서 중복으로 feedback처리를 하지 않도록 메시지 객체에 feedback 정보에 대한 값을 셋팅후 전달
                NSMutableDictionary *userInfo = [self.bestAttemptContent.userInfo mutableCopy];
                [userInfo setObject:@"true" forKey:@"feedback"];
                self.bestAttemptContent.userInfo = userInfo;
                
                [self imagePush:mpsDic];
            }];
        } else {
            [self imagePush:mpsDic];
        }
    }
}

- (void)imagePush:(NSDictionary *)mpsDic {
    NSString *strext = [mpsDic objectForKey:@"ext"];
    NSString *ext;
    
    NSArray *arrayExt = [strext componentsSeparatedByString:@"|"];
    if ([arrayExt count] > 1) {
        self.bestAttemptContent.categoryIdentifier = [arrayExt objectAtIndex:0];
        ext = [arrayExt objectAtIndex:2];
    }
    
    if (ext)
    {
        NSURL *URL = [NSURL URLWithString:ext];
        NSURLSession *LPSession = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
        
        [[LPSession downloadTaskWithURL:URL completionHandler:^(NSURL *temporaryLocation, NSURLResponse *response, NSError *error) {
            if (error) {
                NSLog(@"Leanplum : Error with downloading rich push : %@", [error localizedDescription]);
                self.contentHandler(self.bestAttemptContent);
                return;
            }
            
            NSString *fileType = [self determineType:[response MIMEType]];
            NSString *fileName = [[temporaryLocation.path lastPathComponent] stringByAppendingString:fileType];
            NSString *temporaryDirectory = [NSTemporaryDirectory() stringByAppendingPathComponent:fileName];
            [[NSFileManager defaultManager] moveItemAtPath:temporaryLocation.path toPath:temporaryDirectory error:&error];
            
            NSError *attachmentError = nil;
            UNNotificationAttachment *attachment = [UNNotificationAttachment attachmentWithIdentifier:@""
                                                                                                  URL:[NSURL fileURLWithPath:temporaryDirectory]
                                                                                              options:nil
                                                                                                error:&attachmentError];
            
            if (attachmentError != NULL) {
                NSLog(@"Leanplum : Error with the rich puch attachment : %@", [attachmentError localizedDescription]);
                self.contentHandler(self.bestAttemptContent);
                return;
            }
            self.bestAttemptContent.attachments = @[attachment];
            self.contentHandler(self.bestAttemptContent);
            [[NSFileManager defaultManager] removeItemAtPath:temporaryDirectory error:&error];
        }] resume];
    }
    else
    {
        self.contentHandler(self.bestAttemptContent);
    }
}

- (NSString*)determineType:(NSString *) fileType {
    // Determines the file type of the attachment to append to NSURL.
    if ([fileType isEqualToString:@"image/jpeg"]){
        return @".jpg";
    }
    if ([fileType isEqualToString:@"image/gif"]) {
        return @".gif";
    }
    if ([fileType isEqualToString:@"image/png"]) {
        return @".png";
    } else {
        return @".tmp";
    }
}

- (void)serviceExtensionTimeWillExpire {
    // Called just before the extension will be terminated by the system.
    // Use this as an opportunity to deliver your "best attempt" at modified content, otherwise the original push payload will be used.
    self.contentHandler(self.bestAttemptContent);
}

@end
