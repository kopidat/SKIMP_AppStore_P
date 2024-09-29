//
//  WNInterfaceThirdPartyPattern.h
//

#import <UIKit/UIKit.h>

#import "PatternViewController.h"

@interface WNInterfaceThirdPartyPattern : NSObject<WNInterface, PatternViewControllerDelegate>

@property (strong, nonatomic) NSString *mCallback;
@property (strong, nonatomic) PPWebViewController *viewctrl;

@end
